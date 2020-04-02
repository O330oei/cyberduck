﻿// 
// Copyright (c) 2010-2014 Yves Langisch. All rights reserved.
// http://cyberduck.ch/
// 
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
// 
// Bug fixes, suggestions and comments should be sent to:
// yves@cyberduck.ch
// 

using System;
using ch.cyberduck.core;
using Microsoft.Win32;
using org.apache.log4j;
using ch.cyberduck.core.urlhandler;
using java.util;
using Application = System.Windows.Forms.Application;

namespace Ch.Cyberduck.Core.Urlhandler
{
    public class URLSchemeHandlerConfiguration : AbstractSchemeHandler
    {
        private static readonly Logger Logger = Logger.getLogger(typeof (URLSchemeHandlerConfiguration).FullName);

        public URLSchemeHandlerConfiguration()
        {
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="registry"></param>
        private void RegisterCyberduckUrlHandler(RegistryKey registry)
        {
            CreateCustomUrlHandler(registry, "CyberduckURL", "Cyberduck URL", Application.ExecutablePath,
                Application.ExecutablePath + ",0");
        }

        /// <summary>
        /// Register Cyberduck as default application for the FTP URLs in the current user domain.
        /// </summary>
        private void RegisterFtpProtocol()
        {
            RegisterFtpProtocol(Registry.CurrentUser);
        }

        /// <summary>
        /// Register Cyberduck as default application for the FTP URLs. To make it work with the Windows Search box we need
        /// some more tweaking (e.g. remove the ShellFolder from the ftp entry in the registry).
        /// </summary>
        /// <param name="registry"></param>
        private void RegisterFtpProtocol(RegistryKey registry)
        {
            RegisterCyberduckUrlHandler(registry);
            RegistryKey r =
                registry.CreateSubKey(@"Software\Microsoft\Windows\Shell\Associations\UrlAssociations\ftp\UserChoice");
            r.SetValue("Progid", "CyberduckURL");
            r.Close();
        }

        /// <summary>
        /// Check if Cyberduck is the default application for FTP URLs in the current user domain.
        /// </summary>
        /// <returns></returns>
        private bool IsDefaultApplicationForFtp()
        {
            RegistryKey ftpUserChoice =
                Registry.CurrentUser.OpenSubKey(
                    @"Software\Microsoft\Windows\Shell\Associations\UrlAssociations\ftp\UserChoice");
            return (null != ftpUserChoice && "CyberduckURL".Equals(ftpUserChoice.GetValue("Progid")));
        }

        /// <summary>
        /// Check if Cyberduck is the default application for SFTP URLs in the current user domain.
        /// </summary>
        /// <returns></returns>
        private bool IsDefaultApplicationForSftp()
        {
            RegistryKey sftpClass = Registry.CurrentUser.OpenSubKey(@"Software\Classes\sftp");
            if (null != sftpClass)
            {
                RegistryKey command = sftpClass.OpenSubKey(@"shell\open\command");
                if (null != command)
                {
                    var value = (string) command.GetValue(String.Empty);
                    return (null != value && value.Contains("Cyberduck"));
                }
            }
            return false;
        }

        /// <summary>
        /// Register Cyberduck as default application for the SFTP URLs in the current user domain.
        /// </summary>
        /// <param name="registry"></param>
        private void RegisterSftpProtocol()
        {
            RegisterSftpProtocol(Registry.CurrentUser);
        }

        /// <summary>
        /// Register Cyberduck as default application for the SFTP URLs.
        /// </summary>
        /// <param name="registry"></param>
        private void RegisterSftpProtocol(RegistryKey registry)
        {
            CreateCustomUrlHandler(registry, "sftp", "sftp protocol", Application.ExecutablePath,
                Application.ExecutablePath + ",0");
        }

        private void CreateCustomUrlHandler(RegistryKey registry, string association, string description,
            string applicationPath, string icon)
        {
            RegistryKey r32 = null;
            RegistryKey r64 = null;
            try
            {
                r32 = registry.CreateSubKey(@"SOFTWARE\Classes\" + association);
                r32.SetValue(String.Empty, description);
                r32.SetValue("URL Protocol", String.Empty);

                RegistryKey defaultIcon = r32.CreateSubKey("DefaultIcon");
                defaultIcon.SetValue(String.Empty, applicationPath);

                RegistryKey command = r32.CreateSubKey(@"shell\open\command");
                command.SetValue(String.Empty, "\"" + applicationPath + "\" \"%1\"");

                // If 64-bit OS, also register in the 32-bit registry area. 
                if (registry.OpenSubKey(@"SOFTWARE\Wow6432Node\Classes") != null)
                {
                    r64 = registry.CreateSubKey(@"SOFTWARE\Wow6432Node\Classes" + association);
                    r64.SetValue(String.Empty, description);
                    r64.SetValue("URL Protocol", String.Empty);

                    defaultIcon = r64.CreateSubKey("DefaultIcon");
                    defaultIcon.SetValue(String.Empty, icon);

                    command = r64.CreateSubKey(@"shell\open\command");
                    command.SetValue(String.Empty, "\"" + applicationPath + "\" \"%1\"");
                }
            }
            catch (UnauthorizedAccessException e)
            {
                Logger.error(e.Message);
            }
            finally
            {
                if (null != r32) r32.Close();
                if (null != r64) r64.Close();
            }
        }

        public override void setDefaultHandler(ch.cyberduck.core.local.Application a, List schemes)
        {
            for (int i = 0; i < schemes.size(); i++)
            {
                string scheme = (string) schemes.get(i);
                if(Scheme.ftp.name().Equals(scheme))
                {
                    this.RegisterFtpProtocol();
                }
                else if (Scheme.sftp.name().Equals(scheme))
                {
                    this.RegisterSftpProtocol();
                }
                else
                {
                    CreateCustomUrlHandler(Registry.CurrentUser, scheme, "custom handler", Application.ExecutablePath,
                        Application.ExecutablePath + ",0");
                }
            }
        }

        public override ch.cyberduck.core.local.Application getDefaultHandler(string scheme)
        {
            if (Scheme.ftp.name().Equals(scheme))
            {
                if(this.IsDefaultApplicationForFtp()) return new ch.cyberduck.core.local.Application(Application.ExecutablePath);
            }
            if (Scheme.sftp.name().Equals(scheme))
            {
                if(this.IsDefaultApplicationForSftp()) return new ch.cyberduck.core.local.Application(Application.ExecutablePath);
            }
            return ch.cyberduck.core.local.Application.notfound;
        }

        public override List getAllHandlers(string scheme)
        {
            return Arrays.asList(new ch.cyberduck.core.local.Application(Application.ExecutablePath));
        }
    }
}
