using Nancy;
using Octokit;
using System;
using System.Collections.Generic;
using System.Configuration;
using System.Linq;
using System.Web;

namespace NancyApplication1
{
    public class Handler : NancyModule
    {
        private const string clientId = "be8f08cb9a0471772f88";
        private const string clientSecret = "41f67b9c2ec542449b83039aed56b64a56bf523b";
        private readonly GitHubClient client = new GitHubClient(new ProductHeaderValue("MyHello"));

        public Handler()
        {
            Get["/{user}", true] = async (parms, ct) =>
            {
                var user = await client.User.Get(parms.user.ToString());
                return String.Format("{0} people love {1}!", user.Followers, user.Name);
            };

            // No need to write about this
            Get["/{user}/{repo}/{sha}", true] = async (parms, ct) =>
            {
                var accessToken = Session["accessToken"] as string;
                if (string.IsNullOrEmpty(accessToken))
                    return RedirectToOAuth();
                client.Credentials = new Credentials(accessToken);

                try
                {
                    IEnumerable<CommitStatus> statuses = await client.Repository.CommitStatus.GetAll(
                        parms.user, parms.repo, parms.sha);
                    return string.Join("<br/>",
                        statuses.Select(x => string.Format("{0}: {1}", x.UpdatedAt, x.State)));
                }
                catch (NotFoundException)
                {
                    return HttpStatusCode.NotFound;
                }
            };

            Get["/{user}/{repo}/{sha}/{status}", true] = async (parms, ct) =>
            {
                var accessToken = Session["accessToken"] as string;
                if (string.IsNullOrEmpty(accessToken))
                    return RedirectToOAuth();
                client.Credentials = new Credentials(accessToken);

                CommitState newState = Enum.Parse(typeof(CommitState), parms.status, true);
                try
                {
                    await client.Repository.CommitStatus.Create(
                        parms.user, parms.repo, parms.sha, new NewCommitStatus
                        {
                            State = newState,
                            TargetUrl = new Uri(Request.Url.SiteBase),
                            Context = "arbitrary",
                        });
                }
                catch (NotFoundException)
                {
                    return HttpStatusCode.NotFound;
                }
                return String.Format(
                    @"Done! Go to <a href=""https://api.github.com/repos/{0}/{1}/commits/{2}/status"">this API endpiont</a> " +
                    @"or <a href=""/{0}/{1}/{2}"">the local view</a>",
                    parms.user, parms.repo, parms.sha);
            };

            Get["/authorize", true] = async (parms, ct) =>
            {
                var csrf = Session["CSRF:State"] as string;
                Session.Delete("CSRF:State");
                if (csrf != Request.Query["state"])
                {
                    return HttpStatusCode.Unauthorized;
                }

                var successUrl = ConfigurationManager.AppSettings["baseUrl"] + "/authorize/success";
                var token = await client.Oauth.CreateAccessToken(
                    new OauthTokenRequest(clientId, clientSecret, Request.Query["code"].ToString())
                    {
                        RedirectUri = new Uri(successUrl)
                    });
                Session["accessToken"] = token.AccessToken;
                return Response.AsRedirect(successUrl);
            };

            Get["/authorize/success"] = _ =>
            {
                var origUrl = Session["OrigUrl"].ToString();
                Session.Delete("OrigUrl");
                return Response.AsRedirect(origUrl);
            };
        }

        private Response RedirectToOAuth()
        {
            var csrf = Guid.NewGuid().ToString();
            Session["CSRF:State"] = csrf;
            Session["OrigUrl"] = this.Request.Path;

            var request = new OauthLoginRequest(clientId)
            {
                Scopes = { "repo:status" },
                State = csrf
            };
            var oauthLoginUrl = client.Oauth.GetGitHubLoginUrl(request);
            return Response.AsRedirect(oauthLoginUrl.ToString());
        }
    }
}
