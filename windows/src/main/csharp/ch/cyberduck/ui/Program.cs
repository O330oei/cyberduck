﻿using System;
using System.Collections.Generic;
using System.IO;
using System.IO.Pipes;
using System.Linq;
using System.Reflection;
using System.ServiceModel;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using System.Web;
using System.Windows.Forms;
using ch.cyberduck.core.preferences;
using Ch.Cyberduck.Ui.Controller;
using Ch.Cyberduck.Ui.Core.Contracts;
using Ch.Cyberduck.Ui.Core.Preferences;
using com.google.api.client.util;
using OAuth2AuthorizationService = ch.cyberduck.core.oauth.OAuth2AuthorizationService;

namespace Ch.Cyberduck.Ui
{
    static class Program
    {
        [STAThread]
        static void Main(string[] args)
        {
            bool newInstance;
            Mutex mutex = new Mutex(true, "iterate/cyberduck.io", out newInstance);

            var preferences = new ApplicationPreferences();
            PreferencesFactory.set(preferences);
            var argsTask = Task.Run(async () =>
            {
                using (var channel = new ChannelFactory<ICyberduck>(new NetNamedPipeBinding(), new EndpointAddress("net.pipe://localhost/iterate/cyberduck.io")))
                {
                    ICyberduck proxy = null;

                    // Connect to pipe
                    int retries = 0;
                    bool connected = false;
                    while (!connected && retries < 7) // Timeout after 127 seconds
                    {
                        try
                        {
                            proxy = channel.CreateChannel();
                            proxy.Connect();
                            connected = true;
                        }
                        catch (Exception e)
                        {
                            await Task.Delay(TimeSpan.FromSeconds(Math.Pow(2, retries++)));
                        }
                    }
                    if (!connected)
                    {
                        throw new TimeoutException();
                    }

                    if (!newInstance)
                    {
                        proxy.NewInstance();
                    }

                    foreach (var item in args)
                    {
                        Uri result;
                        if (Uri.TryCreate(item, UriKind.Absolute, out result))
                        {
                            switch (result.Scheme.ToLowerInvariant())
                            {
                                case var scheme when scheme == preferences.getProperty("oauth.handler.scheme"):
                                    if (result.AbsolutePath == "oauth")
                                    {
                                        var query = HttpUtility.ParseQueryString(result.Query);
                                        var state = query.Get("state");
                                        var code = query.Get("code");
                                        proxy.OAuth(state, code);
                                    }
                                    break;

                                case "file":
                                    var localPath = result.LocalPath;
                                    if (result.IsFile)
                                    {
                                        if (File.Exists(localPath))
                                        {
                                            switch (Path.GetExtension(localPath).ToLowerInvariant())
                                            {
                                                case ".cyberducklicense":
                                                    proxy.RegisterRegistration(localPath);
                                                    break;

                                                case ".cyberduckprofile":
                                                    proxy.RegisterProfile(localPath);
                                                    break;

                                                case ".duck":
                                                    proxy.RegisterBookmark(localPath);
                                                    break;
                                            }
                                        }
                                    }
                                    else
                                    {
                                        proxy.QuickConnect(item);
                                    }

                                    break;

                                default:
                                    proxy.QuickConnect(item);
                                    break;
                            }
                        }
                    }
                }
            });

            if (newInstance)
            {
                Application.EnableVisualStyles();
                Application.SetCompatibleTextRenderingDefault(false);

                Application.Run(MainController.Application);
            }
            else
            {
                try
                {
                    argsTask.Wait();
                }
                catch (AggregateException aggregateException)
                {
                    aggregateException.Handle(x =>
                    {
                        if (x is CommunicationObjectFaultedException)
                        {
                            // silent catch this error.
                            return true;
                        }
                        return false;
                    });
                }
            }
            mutex.Close();
        }
    }
}
