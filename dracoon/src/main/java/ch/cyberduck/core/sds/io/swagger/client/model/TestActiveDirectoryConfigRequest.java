/*
 * DRACOON
 * REST Web Services for DRACOON<br>Version: 4.10.7-LTS  - built at: 2019-03-19 14:24:35<br><br><a title='Developer Information' href='https://developer.dracoon.com'>Developer Information</a>&emsp;&emsp;<a title='Get SDKs on GitHub' href='https://github.com/dracoon'>Get SDKs on GitHub</a>
 *
 * OpenAPI spec version: 4.10.7-LTS
 *
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */


package ch.cyberduck.core.sds.io.swagger.client.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

/**
 * TestActiveDirectoryConfigRequest
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2019-09-13T14:25:40.305+02:00")
public class TestActiveDirectoryConfigRequest {
  @JsonProperty("serverIp")
  private String serverIp = null;

  @JsonProperty("serverPort")
  private Integer serverPort = null;

  @JsonProperty("serverAdminName")
  private String serverAdminName = null;

  @JsonProperty("serverAdminPassword")
  private String serverAdminPassword = null;

  @JsonProperty("ldapUsersDomain")
  private String ldapUsersDomain = null;

  @JsonProperty("useLdaps")
  private Boolean useLdaps = null;

  @JsonProperty("sslFingerPrint")
  private String sslFingerPrint = null;

  public TestActiveDirectoryConfigRequest serverIp(String serverIp) {
    this.serverIp = serverIp;
    return this;
  }

   /**
   * IPv4 or IPv6 address or host name
   * @return serverIp
  **/
  @ApiModelProperty(required = true, value = "IPv4 or IPv6 address or host name")
  public String getServerIp() {
    return serverIp;
  }

  public void setServerIp(String serverIp) {
    this.serverIp = serverIp;
  }

  public TestActiveDirectoryConfigRequest serverPort(Integer serverPort) {
    this.serverPort = serverPort;
    return this;
  }

   /**
   * Port
   * @return serverPort
  **/
  @ApiModelProperty(required = true, value = "Port")
  public Integer getServerPort() {
    return serverPort;
  }

  public void setServerPort(Integer serverPort) {
    this.serverPort = serverPort;
  }

  public TestActiveDirectoryConfigRequest serverAdminName(String serverAdminName) {
    this.serverAdminName = serverAdminName;
    return this;
  }

   /**
   * Distinguished Name (DN) of Active Directory administrative account
   * @return serverAdminName
  **/
  @ApiModelProperty(required = true, value = "Distinguished Name (DN) of Active Directory administrative account")
  public String getServerAdminName() {
    return serverAdminName;
  }

  public void setServerAdminName(String serverAdminName) {
    this.serverAdminName = serverAdminName;
  }

  public TestActiveDirectoryConfigRequest serverAdminPassword(String serverAdminPassword) {
    this.serverAdminPassword = serverAdminPassword;
    return this;
  }

   /**
   * Password of Active Directory administrative account
   * @return serverAdminPassword
  **/
  @ApiModelProperty(required = true, value = "Password of Active Directory administrative account")
  public String getServerAdminPassword() {
    return serverAdminPassword;
  }

  public void setServerAdminPassword(String serverAdminPassword) {
    this.serverAdminPassword = serverAdminPassword;
  }

  public TestActiveDirectoryConfigRequest ldapUsersDomain(String ldapUsersDomain) {
    this.ldapUsersDomain = ldapUsersDomain;
    return this;
  }

   /**
   * Search scope of Active Directory; only users below this node can log on.
   * @return ldapUsersDomain
  **/
  @ApiModelProperty(required = true, value = "Search scope of Active Directory; only users below this node can log on.")
  public String getLdapUsersDomain() {
    return ldapUsersDomain;
  }

  public void setLdapUsersDomain(String ldapUsersDomain) {
    this.ldapUsersDomain = ldapUsersDomain;
  }

  public TestActiveDirectoryConfigRequest useLdaps(Boolean useLdaps) {
    this.useLdaps = useLdaps;
    return this;
  }

   /**
   * Determines whether LDAPS should be used instead of plain LDAP.
   * @return useLdaps
  **/
   @ApiModelProperty(example = "false", required = true, value = "Determines whether LDAPS should be used instead of plain LDAP.")
   public Boolean isUseLdaps() {
    return useLdaps;
  }

  public void setUseLdaps(Boolean useLdaps) {
    this.useLdaps = useLdaps;
  }

  public TestActiveDirectoryConfigRequest sslFingerPrint(String sslFingerPrint) {
    this.sslFingerPrint = sslFingerPrint;
    return this;
  }

   /**
   * SSL finger print of Active Directory server. Mandatory for LDAPS connections. Format: &#x60;Algorithm/Fingerprint&#x60;
   * @return sslFingerPrint
  **/
  @ApiModelProperty(value = "SSL finger print of Active Directory server. Mandatory for LDAPS connections. Format: `Algorithm/Fingerprint`")
  public String getSslFingerPrint() {
    return sslFingerPrint;
  }

  public void setSslFingerPrint(String sslFingerPrint) {
    this.sslFingerPrint = sslFingerPrint;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TestActiveDirectoryConfigRequest testActiveDirectoryConfigRequest = (TestActiveDirectoryConfigRequest) o;
    return Objects.equals(this.serverIp, testActiveDirectoryConfigRequest.serverIp) &&
        Objects.equals(this.serverPort, testActiveDirectoryConfigRequest.serverPort) &&
        Objects.equals(this.serverAdminName, testActiveDirectoryConfigRequest.serverAdminName) &&
        Objects.equals(this.serverAdminPassword, testActiveDirectoryConfigRequest.serverAdminPassword) &&
        Objects.equals(this.ldapUsersDomain, testActiveDirectoryConfigRequest.ldapUsersDomain) &&
        Objects.equals(this.useLdaps, testActiveDirectoryConfigRequest.useLdaps) &&
        Objects.equals(this.sslFingerPrint, testActiveDirectoryConfigRequest.sslFingerPrint);
  }

  @Override
  public int hashCode() {
    return Objects.hash(serverIp, serverPort, serverAdminName, serverAdminPassword, ldapUsersDomain, useLdaps, sslFingerPrint);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TestActiveDirectoryConfigRequest {\n");

      sb.append("    serverIp: ").append(toIndentedString(serverIp)).append("\n");
    sb.append("    serverPort: ").append(toIndentedString(serverPort)).append("\n");
    sb.append("    serverAdminName: ").append(toIndentedString(serverAdminName)).append("\n");
    sb.append("    serverAdminPassword: ").append(toIndentedString(serverAdminPassword)).append("\n");
    sb.append("    ldapUsersDomain: ").append(toIndentedString(ldapUsersDomain)).append("\n");
    sb.append("    useLdaps: ").append(toIndentedString(useLdaps)).append("\n");
    sb.append("    sslFingerPrint: ").append(toIndentedString(sslFingerPrint)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

}

