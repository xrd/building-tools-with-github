using Nancy;
using Nancy.Bootstrapper;
using Nancy.Security;
using Nancy.Session;
using Nancy.TinyIoc;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace NancyApplication1
{
    public class Bootstrapper : DefaultNancyBootstrapper
    {
        protected override void ApplicationStartup(TinyIoCContainer container, IPipelines pipelines)
        {
            CookieBasedSessions.Enable(pipelines);
        }
    }
}