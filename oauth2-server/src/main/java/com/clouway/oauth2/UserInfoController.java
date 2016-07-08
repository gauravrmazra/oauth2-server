package com.clouway.oauth2;

import com.clouway.oauth2.http.Request;
import com.clouway.oauth2.http.Response;
import com.clouway.oauth2.http.RsJson;
import com.clouway.oauth2.token.Token;
import com.clouway.oauth2.token.Tokens;
import com.clouway.oauth2.user.IdentityFinder;
import com.google.common.base.Optional;
import com.google.gson.JsonObject;

/**
 * @author Miroslav Genov (miroslav.genov@clouway.com)
 */
class UserInfoController implements InstantaneousRequest {

  private final IdentityFinder identityFinder;
  private final Tokens tokens;

  UserInfoController(IdentityFinder identityFinder, Tokens tokens) {
    this.identityFinder = identityFinder;
    this.tokens = tokens;
  }

  @Override
  public Response handleAsOf(Request request, DateTime instantTime) {
    String accessToken = request.param("access_token");

    Optional<Token> possibleTokenResponse = tokens.getNotExpiredToken(accessToken, instantTime);

    if (!possibleTokenResponse.isPresent()) {
      return OAuthError.invalidToken();
    }

    Token token = possibleTokenResponse.get();

    Optional<Identity> possibleIdentityResponse = identityFinder.findIdentity(token.identityId, instantTime);
    if (!possibleIdentityResponse.isPresent()) {
      return OAuthError.unknownClient();
    }

    Identity identity = possibleIdentityResponse.get();

    JsonObject o = new JsonObject();

    o.addProperty("id", identity.id());
    o.addProperty("name", identity.name());
    o.addProperty("email", identity.email());
    o.addProperty("given_name", identity.givenName());
    o.addProperty("family_name", identity.familyName());

    return new RsJson(o);
  }
}
