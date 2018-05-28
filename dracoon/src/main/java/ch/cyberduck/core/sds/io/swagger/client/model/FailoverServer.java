/*
 * DRACOON
 * REST Web Services for DRACOON<br>Version: 4.8.0-LTS  - built at: 2018-05-03 15:44:37<br><br><a title='Developer Information' href='https://developer.dracoon.com'>Developer Information</a>&emsp;&emsp;<a title='Get SDKs on GitHub' href='https://github.com/dracoon'>Get SDKs on GitHub</a>
 *
 * OpenAPI spec version: 4.8.0-LTS
 * Contact: develop@dracoon.com
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */


package ch.cyberduck.core.sds.io.swagger.client.model;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

/**
 * FailoverServer
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2018-05-23T09:31:14.222+02:00")
public class FailoverServer {
  @JsonProperty("failoverEnabled")
  private Boolean failoverEnabled = null;

  @JsonProperty("failoverIpAddress")
  private String failoverIpAddress = null;

  @JsonProperty("failoverPort")
  private Integer failoverPort = null;

  public FailoverServer failoverEnabled(Boolean failoverEnabled) {
    this.failoverEnabled = failoverEnabled;
    return this;
  }

   /**
   * RADIUS Failover Server is active
   * @return failoverEnabled
  **/
  @ApiModelProperty(example = "false", required = true, value = "RADIUS Failover Server is active")
  public Boolean getFailoverEnabled() {
    return failoverEnabled;
  }

  public void setFailoverEnabled(Boolean failoverEnabled) {
    this.failoverEnabled = failoverEnabled;
  }

  public FailoverServer failoverIpAddress(String failoverIpAddress) {
    this.failoverIpAddress = failoverIpAddress;
    return this;
  }

   /**
   * RADIUS Failover Server IP Address Required if failover server is enabled.
   * @return failoverIpAddress
  **/
  @ApiModelProperty(value = "RADIUS Failover Server IP Address Required if failover server is enabled.")
  public String getFailoverIpAddress() {
    return failoverIpAddress;
  }

  public void setFailoverIpAddress(String failoverIpAddress) {
    this.failoverIpAddress = failoverIpAddress;
  }

  public FailoverServer failoverPort(Integer failoverPort) {
    this.failoverPort = failoverPort;
    return this;
  }

   /**
   * RADIUS Failover Server Port Required if failover server is enabled.
   * @return failoverPort
  **/
  @ApiModelProperty(value = "RADIUS Failover Server Port Required if failover server is enabled.")
  public Integer getFailoverPort() {
    return failoverPort;
  }

  public void setFailoverPort(Integer failoverPort) {
    this.failoverPort = failoverPort;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FailoverServer failoverServer = (FailoverServer) o;
    return Objects.equals(this.failoverEnabled, failoverServer.failoverEnabled) &&
        Objects.equals(this.failoverIpAddress, failoverServer.failoverIpAddress) &&
        Objects.equals(this.failoverPort, failoverServer.failoverPort);
  }

  @Override
  public int hashCode() {
    return Objects.hash(failoverEnabled, failoverIpAddress, failoverPort);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class FailoverServer {\n");

    sb.append("    failoverEnabled: ").append(toIndentedString(failoverEnabled)).append("\n");
    sb.append("    failoverIpAddress: ").append(toIndentedString(failoverIpAddress)).append("\n");
    sb.append("    failoverPort: ").append(toIndentedString(failoverPort)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
  
}
