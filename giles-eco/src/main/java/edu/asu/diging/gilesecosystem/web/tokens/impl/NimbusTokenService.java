package edu.asu.diging.gilesecosystem.web.tokens.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.Issuer;
import com.nimbusds.openid.connect.sdk.Nonce;
import com.nimbusds.openid.connect.sdk.claims.IDTokenClaimsSet;
import com.nimbusds.openid.connect.sdk.validators.IDTokenValidator;

import edu.asu.diging.gilesecosystem.septemberutil.properties.MessageType;
import edu.asu.diging.gilesecosystem.septemberutil.service.ISystemMessageHandler;
import edu.asu.diging.gilesecosystem.util.properties.IPropertiesManager;
import edu.asu.diging.gilesecosystem.web.apps.IRegisteredApp;
import edu.asu.diging.gilesecosystem.web.exceptions.AppMisconfigurationException;
import edu.asu.diging.gilesecosystem.web.exceptions.IdentityProviderMisconfigurationException;
import edu.asu.diging.gilesecosystem.web.exceptions.InvalidTokenException;
import edu.asu.diging.gilesecosystem.web.service.apps.IRegisteredAppManager;
import edu.asu.diging.gilesecosystem.web.service.properties.Properties;
import edu.asu.diging.gilesecosystem.web.tokens.IApiTokenContents;
import edu.asu.diging.gilesecosystem.web.tokens.INimbusTokenService;

@Service
public class NimbusTokenService implements INimbusTokenService {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    @Autowired
    private IPropertiesManager propertyManager;
    
    @Autowired
    private IRegisteredAppManager appsManager;

    @Autowired
    private ISystemMessageHandler messageHandler;
    
    private String issuerUrl;
    
    @PostConstruct
    public void init() {
        issuerUrl = propertyManager.getProperty(Properties.MITREID_SERVER_URL);
        if (!issuerUrl.endsWith("/")) {
            issuerUrl = issuerUrl + "/";
        }
    }

    @Override
    public IApiTokenContents getOpenIdToken(String token, String appId) throws IdentityProviderMisconfigurationException, InvalidTokenException, AppMisconfigurationException {
        JWSAlgorithm jwsAlg = JWSAlgorithm.RS256;
        URL jwkSetURL;
        try {
            jwkSetURL = new URL(issuerUrl + "jwk.json");
        } catch (MalformedURLException e) {
            throw new IdentityProviderMisconfigurationException("Could not download public key.", e);
        }
        
        SignedJWT idToken;
        try {
           idToken  = SignedJWT.parse(token);
        } catch (ParseException e2) {
            throw new InvalidTokenException("Token could not be parse.", e2);
        }
        
        List<String> audiences;
        try {
            JWTClaimsSet claimSet = idToken.getJWTClaimsSet();
            audiences = claimSet.getAudience();
        } catch (ParseException e2) {
            throw new InvalidTokenException("Token could not be parse.", e2);
        }
        
        IRegisteredApp app = appsManager.getApp(appId);
        
        if (app.getProviderClientId() == null || app.getProviderClientId().isEmpty()) {
            throw new AppMisconfigurationException("No provider client id has been registered for your app.");
        }
        
        if (!audiences.contains(app.getProviderClientId())) {
            return null;
        }
        
        Issuer iss = new Issuer(issuerUrl);
        ClientID clientID = new ClientID(app.getProviderClientId());
        IDTokenValidator validator = new IDTokenValidator(iss, clientID, jwsAlg, jwkSetURL);
        
        // No nonce specified
        Nonce expectedNonce = null;
        IDTokenClaimsSet claims = null;

        try {
            claims = validator.validate(idToken, expectedNonce);
        } catch (BadJOSEException e) {
            messageHandler.handleMessage("Token signature or claim is wrong.", e, MessageType.WARNING);
            return null;
        } catch (JOSEException e) {
            messageHandler.handleMessage("Could not validate token.", e, MessageType.ERROR);
            return null;
        }

        if (claims == null) {
            logger.info("No claims provided.");
            return null;
        }
        
        IApiTokenContents tokenContents = new ApiTokenContents();
        tokenContents.setUsername(claims.getSubject().getValue());
        Date expirationTime = claims.getExpirationTime();
        if (expirationTime != null) {
            tokenContents.setExpired(expirationTime.before(new Date()));
        }
            
        return tokenContents;
    }
}
