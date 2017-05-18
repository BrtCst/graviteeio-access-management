/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.am.identityprovider.ldap.authentication;

import io.gravitee.am.identityprovider.api.Authentication;
import io.gravitee.am.identityprovider.api.AuthenticationProvider;
import io.gravitee.am.identityprovider.api.User;
import io.gravitee.am.identityprovider.ldap.LdapIdentityProviderConfiguration;
import io.gravitee.am.identityprovider.ldap.authentication.spring.EmbeddedLdapExecutionListener;
import io.gravitee.am.identityprovider.ldap.authentication.spring.LdapAuthenticationProviderTestConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        LdapAuthenticationProviderTestConfiguration.class,
        LdapAuthenticationProviderTest.LdapAuthenticationConfiguration.class
})
@TestExecutionListeners(
        listeners = { EmbeddedLdapExecutionListener.class,
                DependencyInjectionTestExecutionListener.class })
public class LdapAuthenticationProviderTest {

    @Autowired
    private AuthenticationProvider authenticationProvider;

    @Test
    public void shouldAuthenticate() {
        User user = authenticationProvider.loadUserByUsername(new Authentication() {
            @Override
            public Object getCredentials() {
                return "slashguyspassword";
            }

            @Override
            public Object getPrincipal() {
                return "slashguy";
            }
        });

        Assert.assertNotNull(user);
    }

    @Configuration
    static class LdapAuthenticationConfiguration {

        @Bean
        public LdapIdentityProviderConfiguration configuration() {
            LdapIdentityProviderConfiguration configuration = new LdapIdentityProviderConfiguration();

            configuration.setContextSourceUsername("uid=bob,ou=people,dc=springframework,dc=org");
            configuration.setContextSourcePassword("bobspassword");
            configuration.setContextSourceBase("dc=springframework,dc=org");
            configuration.setContextSourceUrl("ldap://localhost:53389");

            configuration.setUserSearchBase("ou=people");
            configuration.setUserSearchFilter("uid={0}");

            configuration.setGroupSearchBase("ou=GRAVITEE,ou=company,ou=applications");
            configuration.setGroupSearchFilter("member={0}");
            configuration.setGroupRoleAttribute("cn");

            return configuration;
        }

        @Bean
        public AuthenticationProvider authenticationProvider() {
            return new LdapAuthenticationProvider();
        }
    }
}