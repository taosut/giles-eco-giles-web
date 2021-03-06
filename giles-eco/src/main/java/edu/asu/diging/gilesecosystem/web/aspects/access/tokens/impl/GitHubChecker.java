package edu.asu.diging.gilesecosystem.web.aspects.access.tokens.impl;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.social.github.api.GitHubUserProfile;
import org.springframework.social.github.api.impl.GitHubTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import edu.asu.diging.gilesecosystem.septemberutil.properties.MessageType;
import edu.asu.diging.gilesecosystem.septemberutil.service.ISystemMessageHandler;
import edu.asu.diging.gilesecosystem.web.aspects.access.github.GitHubTemplateFactory;
import edu.asu.diging.gilesecosystem.web.aspects.access.openid.google.CheckerResult;
import edu.asu.diging.gilesecosystem.web.aspects.access.openid.google.ValidationResult;
import edu.asu.diging.gilesecosystem.web.aspects.access.tokens.IChecker;
import edu.asu.diging.gilesecosystem.web.exceptions.InvalidTokenException;
import edu.asu.diging.gilesecosystem.web.tokens.IApiTokenContents;
import edu.asu.diging.gilesecosystem.web.tokens.impl.ApiTokenContents;

@Service
public class GitHubChecker implements IChecker {
    
    public final Logger logger = LoggerFactory.getLogger(getClass());

    public final static String ID = "GITHUB";
    
    @Autowired
    private GitHubTemplateFactory templateFactory;

    @Autowired
    private ISystemMessageHandler messageHandler;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public CheckerResult validateToken(String token, String appId) throws GeneralSecurityException,
            IOException, InvalidTokenException {
        
        CheckerResult result = new CheckerResult();
        result.setResult(ValidationResult.INVALID);
        try {
            GitHubTemplate template = templateFactory.createTemplate(token);
            GitHubUserProfile profile = template.userOperations().getUserProfile();
            IApiTokenContents contents = new ApiTokenContents();
            contents.setUsername(profile.getId() + "");
            contents.setExpired(false);
            result.setPayload(contents);
            result.setResult(ValidationResult.VALID);
        } catch (RestClientException ex) {
            messageHandler.handleMessage("Could not authenticate user with GitHub", ex, MessageType.WARNING);
            // validation result already set
        }
        
        return result;
    }

}
