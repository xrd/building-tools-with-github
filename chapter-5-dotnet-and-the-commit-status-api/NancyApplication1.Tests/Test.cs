using NUnit.Framework;
using Nancy;
using Nancy.Testing;
using Nancy.Bootstrapper;
using System.Collections.Generic;
using Nancy.Session;

using NancyApplication1;

namespace NancyApplication1.Tests
{

	public static class BootstrapperExtensions
	{
		public static void WithSession(this IPipelines pipeline,
			                           IDictionary<string, object> session)
		{
			pipeline.BeforeRequest.AddItemToEndOfPipeline(ctx =>
				{
					ctx.Request.Session = new Session(session);
					return null;
				});
		}
	}

	[TestFixture ()]
	public class Test
	{
		private ConfigurableBootstrapper bootstrapper;
		private Browser browser;

		[SetUp]
		public void Setup(){
			this.bootstrapper = new ConfigurableBootstrapper(with => {
				with.Module<Handler>(); 
			});
			this.browser = new Browser (bootstrapper);
		}

		[Test ()]
		public void FetchesUserDetails ()
		{
			var result = this.browser.Get ("/mojombo", 
				with => with.HttpRequest ());
			Assert.AreEqual (HttpStatusCode.OK, result.StatusCode);
			Assert.IsTrue (result.Body.AsString()
				.Contains("Tom Preston-Werner"));
		}

		[Test]
		public void HandlesAuthorization()
		{
			// Mismatched CSRF token
			bootstrapper.WithSession(new Dictionary<string, object> { 
				{ "CSRF:State", "sometoken" }, 
			});
			var result = this.browser.Get ("/authorize", (with) => {
				with.HttpRequest();
				with.Query("state", "someothertoken");
			});
			Assert.AreEqual (HttpStatusCode.Unauthorized, result.StatusCode);

			// Matching CSRF token
			bootstrapper.WithSession(new Dictionary<string, object> { 
				{ "CSRF:State", "sometoken" },
				{ "OrigUrl", "http://success" },
			});
			result = this.browser.Get ("/authorize", (with) => {
				with.HttpRequest();
				with.Query("state", "sometoken");
			});
			result.ShouldHaveRedirectedTo ("/authorize/success");
		}
	}
}

