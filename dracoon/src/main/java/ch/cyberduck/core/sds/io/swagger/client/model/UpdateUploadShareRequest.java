/*
 * DRACOON
 * REST Web Services for DRACOON<br>Version: 4.20.1  - built at: 2020-04-05 23:00:17<br><br><a title='Developer Information' href='https://developer.dracoon.com'>Developer Information</a>&emsp;&emsp;<a title='Get SDKs on GitHub' href='https://github.com/dracoon'>Get SDKs on GitHub</a>
 *
 * OpenAPI spec version: 4.20.1
 * 
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */


package ch.cyberduck.core.sds.io.swagger.client.model;

import java.util.Objects;
import java.util.Arrays;
import ch.cyberduck.core.sds.io.swagger.client.model.ObjectExpiration;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;

/**
 * Request model for updating an Upload Share
 */
@ApiModel(description = "Request model for updating an Upload Share")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2020-04-08T17:57:49.759+02:00")
public class UpdateUploadShareRequest {
  @JsonProperty("name")
  private String name = null;

  @JsonProperty("password")
  private String password = null;

  @JsonProperty("expiration")
  private ObjectExpiration expiration = null;

  @JsonProperty("filesExpiryPeriod")
  private Integer filesExpiryPeriod = null;

  @JsonProperty("internalNotes")
  private String internalNotes = null;

  @JsonProperty("notes")
  private String notes = null;

  @JsonProperty("showCreatorName")
  private Boolean showCreatorName = null;

  @JsonProperty("showCreatorUsername")
  private Boolean showCreatorUsername = null;

  @JsonProperty("notifyCreator")
  private Boolean notifyCreator = null;

  @JsonProperty("showUploadedFiles")
  private Boolean showUploadedFiles = null;

  @JsonProperty("maxSlots")
  private Integer maxSlots = null;

  @JsonProperty("maxSize")
  private Long maxSize = null;

  @JsonProperty("textMessageRecipients")
  private List<String> textMessageRecipients = null;

  @JsonProperty("receiverLanguage")
  private String receiverLanguage = null;

  @JsonProperty("defaultCountry")
  private String defaultCountry = null;

  @JsonProperty("resetPassword")
  private Boolean resetPassword = null;

  @JsonProperty("resetFilesExpiryPeriod")
  private Boolean resetFilesExpiryPeriod = null;

  @JsonProperty("resetMaxSlots")
  private Boolean resetMaxSlots = null;

  @JsonProperty("resetMaxSize")
  private Boolean resetMaxSize = null;

  public UpdateUploadShareRequest name(String name) {
    this.name = name;
    return this;
  }

   /**
   * Alias name
   * @return name
  **/
  @ApiModelProperty(value = "Alias name")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public UpdateUploadShareRequest password(String password) {
    this.password = password;
    return this;
  }

   /**
   * Password
   * @return password
  **/
  @ApiModelProperty(value = "Password")
  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public UpdateUploadShareRequest expiration(ObjectExpiration expiration) {
    this.expiration = expiration;
    return this;
  }

   /**
   * Expiration date / time
   * @return expiration
  **/
  @ApiModelProperty(value = "Expiration date / time")
  public ObjectExpiration getExpiration() {
    return expiration;
  }

  public void setExpiration(ObjectExpiration expiration) {
    this.expiration = expiration;
  }

  public UpdateUploadShareRequest filesExpiryPeriod(Integer filesExpiryPeriod) {
    this.filesExpiryPeriod = filesExpiryPeriod;
    return this;
  }

   /**
   * Number of days after which uploaded files expire
   * @return filesExpiryPeriod
  **/
  @ApiModelProperty(value = "Number of days after which uploaded files expire")
  public Integer getFilesExpiryPeriod() {
    return filesExpiryPeriod;
  }

  public void setFilesExpiryPeriod(Integer filesExpiryPeriod) {
    this.filesExpiryPeriod = filesExpiryPeriod;
  }

  public UpdateUploadShareRequest internalNotes(String internalNotes) {
    this.internalNotes = internalNotes;
    return this;
  }

   /**
   * Internal notes (limited to 255 characters)
   * @return internalNotes
  **/
  @ApiModelProperty(value = "Internal notes (limited to 255 characters)")
  public String getInternalNotes() {
    return internalNotes;
  }

  public void setInternalNotes(String internalNotes) {
    this.internalNotes = internalNotes;
  }

  public UpdateUploadShareRequest notes(String notes) {
    this.notes = notes;
    return this;
  }

   /**
   * User notes (limited to 255 characters)
   * @return notes
  **/
  @ApiModelProperty(value = "User notes (limited to 255 characters)")
  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public UpdateUploadShareRequest showCreatorName(Boolean showCreatorName) {
    this.showCreatorName = showCreatorName;
    return this;
  }

   /**
   * Show creator first and last name.
   * @return showCreatorName
  **/
  @ApiModelProperty(value = "Show creator first and last name.")
  public Boolean isShowCreatorName() {
    return showCreatorName;
  }

  public void setShowCreatorName(Boolean showCreatorName) {
    this.showCreatorName = showCreatorName;
  }

  public UpdateUploadShareRequest showCreatorUsername(Boolean showCreatorUsername) {
    this.showCreatorUsername = showCreatorUsername;
    return this;
  }

   /**
   * Show creator email address.
   * @return showCreatorUsername
  **/
  @ApiModelProperty(value = "Show creator email address.")
  public Boolean isShowCreatorUsername() {
    return showCreatorUsername;
  }

  public void setShowCreatorUsername(Boolean showCreatorUsername) {
    this.showCreatorUsername = showCreatorUsername;
  }

  public UpdateUploadShareRequest notifyCreator(Boolean notifyCreator) {
    this.notifyCreator = notifyCreator;
    return this;
  }

   /**
   * Notify creator on every upload.
   * @return notifyCreator
  **/
  @ApiModelProperty(value = "Notify creator on every upload.")
  public Boolean isNotifyCreator() {
    return notifyCreator;
  }

  public void setNotifyCreator(Boolean notifyCreator) {
    this.notifyCreator = notifyCreator;
  }

  public UpdateUploadShareRequest showUploadedFiles(Boolean showUploadedFiles) {
    this.showUploadedFiles = showUploadedFiles;
    return this;
  }

   /**
   * Allow display of already uploaded files
   * @return showUploadedFiles
  **/
  @ApiModelProperty(value = "Allow display of already uploaded files")
  public Boolean isShowUploadedFiles() {
    return showUploadedFiles;
  }

  public void setShowUploadedFiles(Boolean showUploadedFiles) {
    this.showUploadedFiles = showUploadedFiles;
  }

  public UpdateUploadShareRequest maxSlots(Integer maxSlots) {
    this.maxSlots = maxSlots;
    return this;
  }

   /**
   * Maximal amount of files to upload
   * @return maxSlots
  **/
  @ApiModelProperty(value = "Maximal amount of files to upload")
  public Integer getMaxSlots() {
    return maxSlots;
  }

  public void setMaxSlots(Integer maxSlots) {
    this.maxSlots = maxSlots;
  }

  public UpdateUploadShareRequest maxSize(Long maxSize) {
    this.maxSize = maxSize;
    return this;
  }

   /**
   * Maximal total size of uploaded files (in bytes)
   * @return maxSize
  **/
  @ApiModelProperty(value = "Maximal total size of uploaded files (in bytes)")
  public Long getMaxSize() {
    return maxSize;
  }

  public void setMaxSize(Long maxSize) {
    this.maxSize = maxSize;
  }

  public UpdateUploadShareRequest textMessageRecipients(List<String> textMessageRecipients) {
    this.textMessageRecipients = textMessageRecipients;
    return this;
  }

  public UpdateUploadShareRequest addTextMessageRecipientsItem(String textMessageRecipientsItem) {
    if (this.textMessageRecipients == null) {
      this.textMessageRecipients = new ArrayList<>();
    }
    this.textMessageRecipients.add(textMessageRecipientsItem);
    return this;
  }

   /**
   * List of recipient FQTNs E.123 / E.164 Format
   * @return textMessageRecipients
  **/
  @ApiModelProperty(value = "List of recipient FQTNs E.123 / E.164 Format")
  public List<String> getTextMessageRecipients() {
    return textMessageRecipients;
  }

  public void setTextMessageRecipients(List<String> textMessageRecipients) {
    this.textMessageRecipients = textMessageRecipients;
  }

  public UpdateUploadShareRequest receiverLanguage(String receiverLanguage) {
    this.receiverLanguage = receiverLanguage;
    return this;
  }

   /**
   * Language tag for messages to receiver
   * @return receiverLanguage
  **/
  @ApiModelProperty(example = "de-DE", value = "Language tag for messages to receiver")
  public String getReceiverLanguage() {
    return receiverLanguage;
  }

  public void setReceiverLanguage(String receiverLanguage) {
    this.receiverLanguage = receiverLanguage;
  }

  public UpdateUploadShareRequest defaultCountry(String defaultCountry) {
    this.defaultCountry = defaultCountry;
    return this;
  }

   /**
   * Country shorthand symbol (cf. ISO 3166-2)
   * @return defaultCountry
  **/
  @ApiModelProperty(value = "Country shorthand symbol (cf. ISO 3166-2)")
  public String getDefaultCountry() {
    return defaultCountry;
  }

  public void setDefaultCountry(String defaultCountry) {
    this.defaultCountry = defaultCountry;
  }

  public UpdateUploadShareRequest resetPassword(Boolean resetPassword) {
    this.resetPassword = resetPassword;
    return this;
  }

   /**
   * Set &#39;true&#39; to reset &#39;password&#39; for Upload Share.
   * @return resetPassword
  **/
  @ApiModelProperty(value = "Set 'true' to reset 'password' for Upload Share.")
  public Boolean isResetPassword() {
    return resetPassword;
  }

  public void setResetPassword(Boolean resetPassword) {
    this.resetPassword = resetPassword;
  }

  public UpdateUploadShareRequest resetFilesExpiryPeriod(Boolean resetFilesExpiryPeriod) {
    this.resetFilesExpiryPeriod = resetFilesExpiryPeriod;
    return this;
  }

   /**
   * Set &#39;true&#39; to reset &#39;filesExpiryPeriod&#39; for Upload Share
   * @return resetFilesExpiryPeriod
  **/
  @ApiModelProperty(value = "Set 'true' to reset 'filesExpiryPeriod' for Upload Share")
  public Boolean isResetFilesExpiryPeriod() {
    return resetFilesExpiryPeriod;
  }

  public void setResetFilesExpiryPeriod(Boolean resetFilesExpiryPeriod) {
    this.resetFilesExpiryPeriod = resetFilesExpiryPeriod;
  }

  public UpdateUploadShareRequest resetMaxSlots(Boolean resetMaxSlots) {
    this.resetMaxSlots = resetMaxSlots;
    return this;
  }

   /**
   * Set &#39;true&#39; to reset &#39;maxSlots&#39; for Upload Share
   * @return resetMaxSlots
  **/
  @ApiModelProperty(value = "Set 'true' to reset 'maxSlots' for Upload Share")
  public Boolean isResetMaxSlots() {
    return resetMaxSlots;
  }

  public void setResetMaxSlots(Boolean resetMaxSlots) {
    this.resetMaxSlots = resetMaxSlots;
  }

  public UpdateUploadShareRequest resetMaxSize(Boolean resetMaxSize) {
    this.resetMaxSize = resetMaxSize;
    return this;
  }

   /**
   * Set &#39;true&#39; to reset &#39;maxSize&#39; for Upload Share
   * @return resetMaxSize
  **/
  @ApiModelProperty(value = "Set 'true' to reset 'maxSize' for Upload Share")
  public Boolean isResetMaxSize() {
    return resetMaxSize;
  }

  public void setResetMaxSize(Boolean resetMaxSize) {
    this.resetMaxSize = resetMaxSize;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UpdateUploadShareRequest updateUploadShareRequest = (UpdateUploadShareRequest) o;
    return Objects.equals(this.name, updateUploadShareRequest.name) &&
        Objects.equals(this.password, updateUploadShareRequest.password) &&
        Objects.equals(this.expiration, updateUploadShareRequest.expiration) &&
        Objects.equals(this.filesExpiryPeriod, updateUploadShareRequest.filesExpiryPeriod) &&
        Objects.equals(this.internalNotes, updateUploadShareRequest.internalNotes) &&
        Objects.equals(this.notes, updateUploadShareRequest.notes) &&
        Objects.equals(this.showCreatorName, updateUploadShareRequest.showCreatorName) &&
        Objects.equals(this.showCreatorUsername, updateUploadShareRequest.showCreatorUsername) &&
        Objects.equals(this.notifyCreator, updateUploadShareRequest.notifyCreator) &&
        Objects.equals(this.showUploadedFiles, updateUploadShareRequest.showUploadedFiles) &&
        Objects.equals(this.maxSlots, updateUploadShareRequest.maxSlots) &&
        Objects.equals(this.maxSize, updateUploadShareRequest.maxSize) &&
        Objects.equals(this.textMessageRecipients, updateUploadShareRequest.textMessageRecipients) &&
        Objects.equals(this.receiverLanguage, updateUploadShareRequest.receiverLanguage) &&
        Objects.equals(this.defaultCountry, updateUploadShareRequest.defaultCountry) &&
        Objects.equals(this.resetPassword, updateUploadShareRequest.resetPassword) &&
        Objects.equals(this.resetFilesExpiryPeriod, updateUploadShareRequest.resetFilesExpiryPeriod) &&
        Objects.equals(this.resetMaxSlots, updateUploadShareRequest.resetMaxSlots) &&
        Objects.equals(this.resetMaxSize, updateUploadShareRequest.resetMaxSize);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, password, expiration, filesExpiryPeriod, internalNotes, notes, showCreatorName, showCreatorUsername, notifyCreator, showUploadedFiles, maxSlots, maxSize, textMessageRecipients, receiverLanguage, defaultCountry, resetPassword, resetFilesExpiryPeriod, resetMaxSlots, resetMaxSize);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class UpdateUploadShareRequest {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    password: ").append(toIndentedString(password)).append("\n");
    sb.append("    expiration: ").append(toIndentedString(expiration)).append("\n");
    sb.append("    filesExpiryPeriod: ").append(toIndentedString(filesExpiryPeriod)).append("\n");
    sb.append("    internalNotes: ").append(toIndentedString(internalNotes)).append("\n");
    sb.append("    notes: ").append(toIndentedString(notes)).append("\n");
    sb.append("    showCreatorName: ").append(toIndentedString(showCreatorName)).append("\n");
    sb.append("    showCreatorUsername: ").append(toIndentedString(showCreatorUsername)).append("\n");
    sb.append("    notifyCreator: ").append(toIndentedString(notifyCreator)).append("\n");
    sb.append("    showUploadedFiles: ").append(toIndentedString(showUploadedFiles)).append("\n");
    sb.append("    maxSlots: ").append(toIndentedString(maxSlots)).append("\n");
    sb.append("    maxSize: ").append(toIndentedString(maxSize)).append("\n");
    sb.append("    textMessageRecipients: ").append(toIndentedString(textMessageRecipients)).append("\n");
    sb.append("    receiverLanguage: ").append(toIndentedString(receiverLanguage)).append("\n");
    sb.append("    defaultCountry: ").append(toIndentedString(defaultCountry)).append("\n");
    sb.append("    resetPassword: ").append(toIndentedString(resetPassword)).append("\n");
    sb.append("    resetFilesExpiryPeriod: ").append(toIndentedString(resetFilesExpiryPeriod)).append("\n");
    sb.append("    resetMaxSlots: ").append(toIndentedString(resetMaxSlots)).append("\n");
    sb.append("    resetMaxSize: ").append(toIndentedString(resetMaxSize)).append("\n");
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

