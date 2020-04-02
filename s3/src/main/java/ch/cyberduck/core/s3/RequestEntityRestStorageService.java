package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
 * http://cyberduck.ch/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.PreferencesUseragentProvider;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;
import org.jets3t.service.Constants;
import org.jets3t.service.Jets3tProperties;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.ServiceException;
import org.jets3t.service.StorageObjectsChunk;
import org.jets3t.service.VersionOrDeleteMarkersChunk;
import org.jets3t.service.impl.rest.XmlResponsesSaxParser;
import org.jets3t.service.impl.rest.httpclient.HttpMethodReleaseInputStream;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.BaseVersionOrDeleteMarker;
import org.jets3t.service.model.StorageBucketLoggingStatus;
import org.jets3t.service.model.StorageObject;
import org.jets3t.service.model.WebsiteConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestEntityRestStorageService extends RestS3Service {
    private static final Logger log = Logger.getLogger(RequestEntityRestStorageService.class);

    private final S3Session session;

    private final Preferences preferences
        = PreferencesFactory.get();

    public RequestEntityRestStorageService(final S3Session session,
                                           final Jets3tProperties properties,
                                           final HttpClientBuilder configuration) {
        super(null, new PreferencesUseragentProvider().get(), null, properties);
        this.session = session;
        configuration.disableContentCompression();
        configuration.setRetryHandler(new S3HttpRequestRetryHandler(this, preferences.getInteger("http.connections.retry")));
        configuration.setRedirectStrategy(new DefaultRedirectStrategy() {
            @Override
            public HttpUriRequest getRedirect(final HttpRequest request, final HttpResponse response, final HttpContext context) throws ProtocolException {
                if(response.containsHeader("x-amz-bucket-region")) {
                    final String host = ((HttpUriRequest) request).getURI().getHost();
                    if(!StringUtils.equals(session.getHost().getHostname(), host)) {
                        regionEndpointCache.putRegionForBucketName(
                            StringUtils.split(StringUtils.removeEnd(((HttpUriRequest) request).getURI().getHost(), session.getHost().getHostname()), ".")[0],
                            response.getFirstHeader("x-amz-bucket-region").getValue());
                    }
                }
                return super.getRedirect(request, response, context);
            }
        });
        this.setHttpClient(configuration.build());
    }

    @Override
    protected void initializeDefaults() {
        //
    }

    @Override
    protected HttpClientBuilder initHttpClientBuilder() {
        return null;
    }


    @Override
    protected void initializeProxy(final HttpClientBuilder httpClientBuilder) {
        //
    }

    @Override
    protected HttpUriRequest setupConnection(final HTTP_METHOD method, final String bucketName,
                                             final String objectKey, final Map<String, String> requestParameters)
        throws S3ServiceException {
        final HttpUriRequest request = super.setupConnection(method, bucketName, objectKey, requestParameters);
        if(preferences.getBoolean("s3.upload.expect-continue")) {
            if("PUT".equals(request.getMethod())) {
                // #7621
                final Jets3tProperties properties = getJetS3tProperties();
                if(!properties.getBoolProperty("s3service.disable-expect-continue", false)) {
                    request.addHeader(HTTP.EXPECT_DIRECTIVE, HTTP.EXPECT_CONTINUE);
                }
            }
        }
        if(preferences.getBoolean("s3.bucket.requesterpays")) {
            // Only for AWS
            if(S3Session.isAwsHostname(session.getHost().getHostname())) {
                // Downloading Objects in Requester Pays Buckets
                if("GET".equals(request.getMethod()) || "POST".equals(request.getMethod())) {
                    final Jets3tProperties properties = getJetS3tProperties();
                    if(!properties.getBoolProperty("s3service.disable-request-payer", false)) {
                        // For GET and POST requests, include x-amz-request-payer : requester in the header
                        request.addHeader("x-amz-request-payer", "requester");
                    }
                }
            }
        }
        return request;
    }

    @Override
    protected boolean isTargettingGoogleStorageService() {
        return session.getHost().getHostname().equals(Constants.GS_DEFAULT_HOSTNAME);
    }

    @Override
    public void putObjectWithRequestEntityImpl(String bucketName, StorageObject object,
                                               HttpEntity requestEntity, Map<String, String> requestParams) throws ServiceException {
        super.putObjectWithRequestEntityImpl(bucketName, object, requestEntity, requestParams);
    }

    @Override
    public StorageObject getObjectImpl(boolean headOnly, String bucketName, String objectKey,
                                       Calendar ifModifiedSince, Calendar ifUnmodifiedSince,
                                       String[] ifMatchTags, String[] ifNoneMatchTags,
                                       Long byteRangeStart, Long byteRangeEnd, String versionId,
                                       Map<String, Object> requestHeaders,
                                       Map<String, String> requestParameters) throws ServiceException {
        return super.getObjectImpl(headOnly, bucketName, objectKey, ifModifiedSince, ifUnmodifiedSince, ifMatchTags, ifNoneMatchTags, byteRangeStart, byteRangeEnd,
            versionId, requestHeaders, requestParameters);
    }

    @Override
    protected StorageObjectsChunk listObjectsInternal(
        String bucketName, String prefix, String delimiter, long maxListingLength,
        boolean automaticallyMergeChunks, String priorLastKey) throws ServiceException {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("encoding-type", "url");
        if(prefix != null) {
            parameters.put("prefix", prefix);
        }
        if(delimiter != null) {
            parameters.put("delimiter", delimiter);
        }
        if(maxListingLength > 0) {
            parameters.put("max-keys", String.valueOf(maxListingLength));
        }

        List<StorageObject> objects = new ArrayList<StorageObject>();
        List<String> commonPrefixes = new ArrayList<String>();

        boolean incompleteListing = true;
        int ioErrorRetryCount = 0;

        while(incompleteListing) {
            if(priorLastKey != null) {
                parameters.put("marker", priorLastKey);
            }
            else {
                parameters.remove("marker");
            }

            HttpResponse httpResponse = performRestGet(bucketName, null, parameters, null);
            XmlResponsesSaxParser.ListBucketHandler listBucketHandler;

            try {
                listBucketHandler = getXmlResponseSaxParser()
                    .parseListBucketResponse(
                        new HttpMethodReleaseInputStream(httpResponse));
                ioErrorRetryCount = 0;
            }
            catch(ServiceException e) {
                if(e.getCause() instanceof IOException && ioErrorRetryCount < 5) {
                    ioErrorRetryCount++;
                    log.warn("Retrying bucket listing failure due to IO error", e);
                    continue;
                }
                else {
                    throw e;
                }
            }

            StorageObject[] partialObjects = listBucketHandler.getObjects();
            if(log.isDebugEnabled()) {
                log.debug("Found " + partialObjects.length + " objects in one batch");
            }
            objects.addAll(Arrays.asList(partialObjects));

            String[] partialCommonPrefixes = listBucketHandler.getCommonPrefixes();
            if(log.isDebugEnabled()) {
                log.debug("Found " + partialCommonPrefixes.length + " common prefixes in one batch");
            }
            commonPrefixes.addAll(Arrays.asList(partialCommonPrefixes));

            incompleteListing = listBucketHandler.isListingTruncated();
            if(incompleteListing) {
                priorLastKey = listBucketHandler.getMarkerForNextListing();
                if(log.isDebugEnabled()) {
                    log.debug("Yet to receive complete listing of bucket contents, "
                        + "last key for prior chunk: " + priorLastKey);
                }
            }
            else {
                priorLastKey = null;
            }

            if(!automaticallyMergeChunks) {
                break;
            }
        }
        if(automaticallyMergeChunks) {
            if(log.isDebugEnabled()) {
                log.debug("Found " + objects.size() + " objects in total");
            }
            return new StorageObjectsChunk(
                prefix, delimiter,
                objects.toArray(new StorageObject[objects.size()]),
                commonPrefixes.toArray(new String[commonPrefixes.size()]),
                null);
        }
        else {
            return new StorageObjectsChunk(
                prefix, delimiter,
                objects.toArray(new StorageObject[objects.size()]),
                commonPrefixes.toArray(new String[commonPrefixes.size()]),
                priorLastKey);
        }
    }

    @Override
    protected VersionOrDeleteMarkersChunk listVersionedObjectsInternal(
        String bucketName, String prefix, String delimiter, long maxListingLength,
        boolean automaticallyMergeChunks, String nextKeyMarker, String nextVersionIdMarker) throws S3ServiceException {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("encoding-type", "url");
        parameters.put("versions", null);
        if(prefix != null) {
            parameters.put("prefix", prefix);
        }
        if(delimiter != null) {
            parameters.put("delimiter", delimiter);
        }
        if(maxListingLength > 0) {
            parameters.put("max-keys", String.valueOf(maxListingLength));
        }

        List<BaseVersionOrDeleteMarker> items = new ArrayList<BaseVersionOrDeleteMarker>();
        List<String> commonPrefixes = new ArrayList<String>();

        boolean incompleteListing = true;
        int ioErrorRetryCount = 0;

        while(incompleteListing) {
            if(nextKeyMarker != null) {
                parameters.put("key-marker", nextKeyMarker);
            }
            else {
                parameters.remove("key-marker");
            }
            if(nextVersionIdMarker != null) {
                parameters.put("version-id-marker", nextVersionIdMarker);
            }
            else {
                parameters.remove("version-id-marker");
            }

            HttpResponse httpResponse;
            try {
                httpResponse = performRestGet(bucketName, null, parameters, null);
            }
            catch(ServiceException se) {
                throw new S3ServiceException(se);
            }
            XmlResponsesSaxParser.ListVersionsResultsHandler handler;

            try {
                handler = getXmlResponseSaxParser()
                    .parseListVersionsResponse(
                        new HttpMethodReleaseInputStream(httpResponse));
                ioErrorRetryCount = 0;
            }
            catch(ServiceException se) {
                if(se.getCause() instanceof IOException && ioErrorRetryCount < 5) {
                    ioErrorRetryCount++;
                    log.warn("Retrying bucket listing failure due to IO error", se);
                    continue;
                }
                else {
                    throw new S3ServiceException(se);
                }
            }

            BaseVersionOrDeleteMarker[] partialItems = handler.getItems();
            if(log.isDebugEnabled()) {
                log.debug("Found " + partialItems.length + " items in one batch");
            }
            items.addAll(Arrays.asList(partialItems));

            String[] partialCommonPrefixes = handler.getCommonPrefixes();
            if(log.isDebugEnabled()) {
                log.debug("Found " + partialCommonPrefixes.length + " common prefixes in one batch");
            }
            commonPrefixes.addAll(Arrays.asList(partialCommonPrefixes));

            incompleteListing = handler.isListingTruncated();
            nextKeyMarker = handler.getNextKeyMarker();
            nextVersionIdMarker = handler.getNextVersionIdMarker();
            if(incompleteListing) {
                if(log.isDebugEnabled()) {
                    log.debug("Yet to receive complete listing of bucket versions, "
                        + "continuing with key-marker=" + nextKeyMarker
                        + " and version-id-marker=" + nextVersionIdMarker);
                }
            }

            if(!automaticallyMergeChunks) {
                break;
            }
        }
        if(automaticallyMergeChunks) {
            if(log.isDebugEnabled()) {
                log.debug("Found " + items.size() + " items in total");
            }
            return new VersionOrDeleteMarkersChunk(
                prefix, delimiter,
                items.toArray(new BaseVersionOrDeleteMarker[items.size()]),
                commonPrefixes.toArray(new String[commonPrefixes.size()]),
                null, null);
        }
        else {
            return new VersionOrDeleteMarkersChunk(
                prefix, delimiter,
                items.toArray(new BaseVersionOrDeleteMarker[items.size()]),
                commonPrefixes.toArray(new String[commonPrefixes.size()]),
                nextKeyMarker, nextVersionIdMarker);
        }
    }

    @Override
    public void verifyExpectedAndActualETagValues(String expectedETag, StorageObject uploadedObject) throws ServiceException {
        if(StringUtils.isBlank(uploadedObject.getETag())) {
            log.warn("No ETag to verify");
            return;
        }
        super.verifyExpectedAndActualETagValues(expectedETag, uploadedObject);
    }

    /**
     * @return the identifier for the signature algorithm.
     */
    @Override
    protected String getSignatureIdentifier() {
        return session.getSignatureIdentifier();
    }

    /**
     * @return header prefix for general Google Storage headers: x-goog-.
     */
    @Override
    public String getRestHeaderPrefix() {
        return session.getRestHeaderPrefix();
    }

    /**
     * @return header prefix for Google Storage metadata headers: x-goog-meta-.
     */
    @Override
    public String getRestMetadataPrefix() {
        return session.getRestMetadataPrefix();
    }

    @Override
    protected XmlResponsesSaxParser getXmlResponseSaxParser() throws ServiceException {
        return session.getXmlResponseSaxParser();
    }

    @Override
    public void setBucketLoggingStatusImpl(String bucketName, StorageBucketLoggingStatus status) throws ServiceException {
        super.setBucketLoggingStatusImpl(bucketName, status);
    }

    @Override
    public StorageBucketLoggingStatus getBucketLoggingStatusImpl(String bucketName) throws ServiceException {
        return super.getBucketLoggingStatusImpl(bucketName);
    }

    @Override
    public WebsiteConfig getWebsiteConfigImpl(String bucketName) throws ServiceException {
        return super.getWebsiteConfigImpl(bucketName);
    }

    @Override
    public void setWebsiteConfigImpl(String bucketName, WebsiteConfig config) throws ServiceException {
        super.setWebsiteConfigImpl(bucketName, config);
    }

    @Override
    public void deleteWebsiteConfigImpl(String bucketName) throws ServiceException {
        super.deleteWebsiteConfigImpl(bucketName);
    }

    @Override
    public void authorizeHttpRequest(final HttpUriRequest httpMethod, final HttpContext context,
                                     final String forceRequestSignatureVersion) throws ServiceException {
        if(forceRequestSignatureVersion != null) {
            final S3Protocol.AuthenticationHeaderSignatureVersion authenticationHeaderSignatureVersion
                = S3Protocol.AuthenticationHeaderSignatureVersion.valueOf(StringUtils.remove(forceRequestSignatureVersion, "-"));
            log.warn(String.format("Switched authentication signature version to %s", forceRequestSignatureVersion));
            session.setSignatureVersion(authenticationHeaderSignatureVersion);
        }
        super.authorizeHttpRequest(httpMethod, context, forceRequestSignatureVersion);
    }

    @Override
    protected boolean isXmlContentType(final String contentType) {
        if(null == contentType) {
            return false;
        }
        if(StringUtils.startsWithIgnoreCase(contentType, "application/xml")) {
            return true;
        }
        if(StringUtils.startsWithIgnoreCase(contentType, "text/xml")) {
            return true;
        }
        return false;
    }
}
