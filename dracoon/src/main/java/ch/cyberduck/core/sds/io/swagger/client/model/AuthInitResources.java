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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * DEPRECATED
 */
@ApiModel(description = "DEPRECATED")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2019-09-13T14:25:40.305+02:00")
public class AuthInitResources {
  @JsonProperty("authTypes")
  private List<KeyValueEntry> authTypes = new ArrayList<>();

  @JsonProperty("languages")
  private List<Language> languages = new ArrayList<>();

  public AuthInitResources authTypes(List<KeyValueEntry> authTypes) {
    this.authTypes = authTypes;
    return this;
  }

  public AuthInitResources addAuthTypesItem(KeyValueEntry authTypesItem) {
    this.authTypes.add(authTypesItem);
    return this;
  }

   /**
   * Authentication methods: * &#x60;sql&#x60; * &#x60;active_directory&#x60; * &#x60;radius&#x60; * &#x60;openid&#x60;
   * @return authTypes
  **/
  @ApiModelProperty(required = true, value = "Authentication methods: * `sql` * `active_directory` * `radius` * `openid`")
  public List<KeyValueEntry> getAuthTypes() {
    return authTypes;
  }

  public void setAuthTypes(List<KeyValueEntry> authTypes) {
    this.authTypes = authTypes;
  }

  public AuthInitResources languages(List<Language> languages) {
    this.languages = languages;
    return this;
  }

  public AuthInitResources addLanguagesItem(Language languagesItem) {
    this.languages.add(languagesItem);
    return this;
  }

   /**
    * &#x60;DEPRECATED&#x60;: Supported languages  [Deprecated since version 4.7.0]
   * @return languages
  **/
   @ApiModelProperty(required = true, value = "`DEPRECATED`: Supported languages  [Deprecated since version 4.7.0]")
  public List<Language> getLanguages() {
    return languages;
  }

  public void setLanguages(List<Language> languages) {
    this.languages = languages;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AuthInitResources authInitResources = (AuthInitResources) o;
    return Objects.equals(this.authTypes, authInitResources.authTypes) &&
        Objects.equals(this.languages, authInitResources.languages);
  }

  @Override
  public int hashCode() {
    return Objects.hash(authTypes, languages);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AuthInitResources {\n");

      sb.append("    authTypes: ").append(toIndentedString(authTypes)).append("\n");
    sb.append("    languages: ").append(toIndentedString(languages)).append("\n");
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

