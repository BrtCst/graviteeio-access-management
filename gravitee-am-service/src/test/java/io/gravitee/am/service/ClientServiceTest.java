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
package io.gravitee.am.service;

import io.gravitee.am.model.Client;
import io.gravitee.am.model.Domain;
import io.gravitee.am.model.IdentityProvider;
import io.gravitee.am.model.common.Page;
import io.gravitee.am.repository.exceptions.TechnicalException;
import io.gravitee.am.repository.management.api.ClientRepository;
import io.gravitee.am.service.exception.ClientAlreadyExistsException;
import io.gravitee.am.service.exception.ClientNotFoundException;
import io.gravitee.am.service.exception.TechnicalManagementException;
import io.gravitee.am.service.impl.ClientServiceImpl;
import io.gravitee.am.service.model.NewClient;
import io.gravitee.am.service.model.TotalClient;
import io.gravitee.am.service.model.UpdateClient;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
@RunWith(MockitoJUnitRunner.class)
public class ClientServiceTest {

    @InjectMocks
    private ClientService clientService = new ClientServiceImpl();

    @Mock
    private DomainService domainService;

    @Mock
    private IdentityProviderService identityProviderService;

    @Mock
    private ClientRepository clientRepository;

    private final static String DOMAIN = "domain1";

    @Test
    public void shouldFindById() {
        when(clientRepository.findById("my-client")).thenReturn(Maybe.just(new Client()));
        TestObserver testObserver = clientService.findById("my-client").test();

        testObserver.awaitTerminalEvent();
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValueCount(1);
    }

    @Test
    public void shouldFindById_notExistingClient() {
        when(clientRepository.findById("my-client")).thenReturn(Maybe.empty());
        TestObserver testObserver = clientService.findById("my-client").test();
        testObserver.awaitTerminalEvent();

        testObserver.assertNoValues();
    }

    @Test
    public void shouldFindById_technicalException() {
        when(clientRepository.findById("my-client")).thenReturn(Maybe.error(TechnicalException::new));
        TestObserver testObserver = new TestObserver();
        clientService.findById("my-client").subscribe(testObserver);

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldFindByDomainAndClientId() {
        when(clientRepository.findByClientIdAndDomain("my-client", DOMAIN)).thenReturn(Maybe.just(new Client()));
        TestObserver testObserver = clientService.findByDomainAndClientId(DOMAIN, "my-client").test();

        testObserver.awaitTerminalEvent();
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValueCount(1);
    }

    @Test
    public void findByClientIdAndDomain() {
        when(clientRepository.findByClientIdAndDomain("my-client", DOMAIN)).thenReturn(Maybe.empty());
        TestObserver testObserver = clientService.findByDomainAndClientId(DOMAIN, "my-client").test();
        testObserver.awaitTerminalEvent();

        testObserver.assertNoValues();
    }

    @Test
    public void shouldFindByDomainAndClientId_technicalException() {
        when(clientRepository.findByClientIdAndDomain("my-client", DOMAIN)).thenReturn(Maybe.error(TechnicalException::new));
        TestObserver testObserver = new TestObserver();
        clientService.findByDomainAndClientId(DOMAIN, "my-client").subscribe(testObserver);

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldFindByDomain() {
        when(clientRepository.findByDomain(DOMAIN)).thenReturn(Single.just(Collections.singleton(new Client())));
        TestObserver<Set<Client>> testObserver = clientService.findByDomain(DOMAIN).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(extensionGrants -> extensionGrants.size() == 1);
    }

    @Test
    public void shouldFindByDomain_technicalException() {
        when(clientRepository.findByDomain(DOMAIN)).thenReturn(Single.error(TechnicalException::new));

        TestObserver testObserver = new TestObserver<>();
        clientService.findByDomain(DOMAIN).subscribe(testObserver);

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldFindByDomainPagination() {
        Page pageClients = new Page(Collections.singleton(new Client()), 1 , 1);
        when(clientRepository.findByDomain(DOMAIN, 1 , 1)).thenReturn(Single.just(pageClients));
        TestObserver<Page<Client>> testObserver = clientService.findByDomain(DOMAIN, 1, 1).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(extensionGrants -> extensionGrants.getData().size() == 1);
    }

    @Test
    public void shouldFindByDomainPagination_technicalException() {
        when(clientRepository.findByDomain(DOMAIN, 1 , 1)).thenReturn(Single.error(TechnicalException::new));

        TestObserver testObserver = new TestObserver<>();
        clientService.findByDomain(DOMAIN, 1 , 1).subscribe(testObserver);

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldFindByIdentityProvider() {
        when(clientRepository.findByIdentityProvider("client-idp")).thenReturn(Single.just(Collections.singleton(new Client())));
        TestObserver<Set<Client>> testObserver = clientService.findByIdentityProvider("client-idp").test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(extensionGrants -> extensionGrants.size() == 1);
    }

    @Test
    public void shouldFindByIdentityProvider_technicalException() {
        when(clientRepository.findByIdentityProvider("client-idp")).thenReturn(Single.error(TechnicalException::new));

        TestObserver testObserver = new TestObserver<>();
        clientService.findByIdentityProvider("client-idp").subscribe(testObserver);

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldFindByCertificate() {
        when(clientRepository.findByCertificate("client-certificate")).thenReturn(Single.just(Collections.singleton(new Client())));
        TestObserver<Set<Client>> testObserver = clientService.findByCertificate("client-certificate").test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(extensionGrants -> extensionGrants.size() == 1);
    }

    @Test
    public void shouldFindByCertificate_technicalException() {
        when(clientRepository.findByCertificate("client-certificate")).thenReturn(Single.error(TechnicalException::new));

        TestObserver testObserver = new TestObserver<>();
        clientService.findByCertificate("client-certificate").subscribe(testObserver);

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldFindByExtensionGrant() {
        when(clientRepository.findByExtensionGrant("client-extension-grant")).thenReturn(Single.just(Collections.singleton(new Client())));
        TestObserver<Set<Client>> testObserver = clientService.findByExtensionGrant("client-extension-grant").test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(extensionGrants -> extensionGrants.size() == 1);
    }

    @Test
    public void shouldFindByExtensionGrant_technicalException() {
        when(clientRepository.findByExtensionGrant("client-extension-grant")).thenReturn(Single.error(TechnicalException::new));

        TestObserver testObserver = new TestObserver<>();
        clientService.findByExtensionGrant("client-extension-grant").subscribe(testObserver);

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldFindAll() {
        when(clientRepository.findAll()).thenReturn(Single.just(Collections.singleton(new Client())));
        TestObserver<Set<Client>> testObserver = clientService.findAll().test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(extensionGrants -> extensionGrants.size() == 1);
    }

    @Test
    public void shouldFindAll_technicalException() {
        when(clientRepository.findAll()).thenReturn(Single.error(TechnicalException::new));

        TestObserver testObserver = new TestObserver<>();
        clientService.findAll().subscribe(testObserver);

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldFindAllPagination() {
        Page pageClients = new Page(Collections.singleton(new Client()), 1 , 1);
        when(clientRepository.findAll(1 , 1)).thenReturn(Single.just(pageClients));
        TestObserver<Page<Client>> testObserver = clientService.findAll(1, 1).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(extensionGrants -> extensionGrants.getData().size() == 1);
    }

    @Test
    public void shouldFindAllPagination_technicalException() {
        when(clientRepository.findAll(1 , 1)).thenReturn(Single.error(TechnicalException::new));

        TestObserver testObserver = new TestObserver<>();
        clientService.findAll(1 , 1).subscribe(testObserver);

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldFindTotalClientsByDomain() {
        when(clientRepository.countByDomain(DOMAIN)).thenReturn(Single.just(1l));
        TestObserver<TotalClient> testObserver = clientService.findTotalClientsByDomain(DOMAIN).test();

        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(totalClient -> totalClient.getTotalClients() == 1l);
    }

    @Test
    public void shouldFindTotalClientsByDomain_technicalException() {
        when(clientRepository.countByDomain(DOMAIN)).thenReturn(Single.error(TechnicalException::new));

        TestObserver testObserver = new TestObserver<>();
        clientService.findTotalClientsByDomain(DOMAIN).subscribe(testObserver);

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldFindTotalClients() {
        when(clientRepository.count()).thenReturn(Single.just(1l));
        TestObserver<TotalClient> testObserver = clientService.findTotalClients().test();

        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(totalClient -> totalClient.getTotalClients() == 1l);
    }

    @Test
    public void shouldFindTotalClients_technicalException() {
        when(clientRepository.count()).thenReturn(Single.error(TechnicalException::new));

        TestObserver testObserver = new TestObserver<>();
        clientService.findTotalClients().subscribe(testObserver);

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldCreate() {
        NewClient newClient = Mockito.mock(NewClient.class);
        when(newClient.getClientId()).thenReturn("my-client");
        when(clientRepository.findByClientIdAndDomain("my-client", DOMAIN)).thenReturn(Maybe.empty());
        when(clientRepository.create(any(Client.class))).thenReturn(Single.just(new Client()));
        when(domainService.reload(eq(DOMAIN), any())).thenReturn(Single.just(new Domain()));

        TestObserver testObserver = clientService.create(DOMAIN, newClient).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(clientRepository, times(1)).findByClientIdAndDomain(anyString(), anyString());
        verify(clientRepository, times(1)).create(any(Client.class));
    }

    @Test
    public void shouldCreate_technicalException() {
        NewClient newClient = Mockito.mock(NewClient.class);
        when(newClient.getClientId()).thenReturn("my-client");
        when(clientRepository.findByClientIdAndDomain( "my-client", DOMAIN)).thenReturn(Maybe.error(TechnicalException::new));

        TestObserver<Client> testObserver = new TestObserver<>();
        clientService.create(DOMAIN, newClient).subscribe(testObserver);

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();

        verify(clientRepository, never()).create(any(Client.class));
    }

    @Test
    public void shouldCreate2_technicalException() {
        NewClient newClient = Mockito.mock(NewClient.class);
        when(newClient.getClientId()).thenReturn("my-client");
        when(clientRepository.findByClientIdAndDomain("my-client", DOMAIN)).thenReturn(Maybe.empty());
        when(clientRepository.create(any(Client.class))).thenReturn(Single.error(TechnicalException::new));

        TestObserver<Client> testObserver = new TestObserver<>();
        clientService.create(DOMAIN, newClient).subscribe(testObserver);

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();

        verify(clientRepository, times(1)).findByClientIdAndDomain(anyString(), anyString());
    }

    @Test
    public void shouldCreate_clientAlreadyExists() {
        NewClient newClient = Mockito.mock(NewClient.class);
        when(newClient.getClientId()).thenReturn("my-client");
        when(clientRepository.findByClientIdAndDomain("my-client", DOMAIN)).thenReturn(Maybe.just(new Client()));

        TestObserver<Client> testObserver = new TestObserver<>();
        clientService.create(DOMAIN, newClient).subscribe(testObserver);

        testObserver.assertError(ClientAlreadyExistsException.class);
        testObserver.assertNotComplete();

        verify(clientRepository, times(1)).findByClientIdAndDomain(anyString(), anyString());
        verify(clientRepository, never()).create(any(Client.class));
    }

    @Test
    public void shouldUpdate() {
        UpdateClient updateClient = Mockito.mock(UpdateClient.class);
        when(updateClient.getIdentities()).thenReturn(new HashSet<>(Arrays.asList("id1", "id2")));
        when(clientRepository.findById("my-client")).thenReturn(Maybe.just(new Client()));
        when(identityProviderService.findById("id1")).thenReturn(Maybe.just(new IdentityProvider()));
        when(identityProviderService.findById("id2")).thenReturn(Maybe.just(new IdentityProvider()));
        when(clientRepository.update(any(Client.class))).thenReturn(Single.just(new Client()));
        when(domainService.reload(eq(DOMAIN), any())).thenReturn(Single.just(new Domain()));

        TestObserver testObserver = clientService.update(DOMAIN, "my-client", updateClient).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(clientRepository, times(1)).findById(anyString());
        verify(identityProviderService, times(2)).findById(anyString());
        verify(clientRepository, times(1)).update(any(Client.class));
    }

    @Test
    public void shouldUpdate_technicalException() {
        UpdateClient updateClient = Mockito.mock(UpdateClient.class);
        when(clientRepository.findById("my-client")).thenReturn(Maybe.error(TechnicalException::new));

        TestObserver testObserver = clientService.update(DOMAIN, "my-client", updateClient).test();
        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();

        verify(clientRepository, times(1)).findById(anyString());
        verify(clientRepository, never()).update(any(Client.class));
    }

    @Test
    public void shouldUpdate2_technicalException() {
        UpdateClient updateClient = Mockito.mock(UpdateClient.class);
        when(clientRepository.findById("my-client")).thenReturn(Maybe.just(new Client()));
        when(clientRepository.update(any(Client.class))).thenReturn(Single.error(TechnicalException::new));

        TestObserver testObserver = clientService.update(DOMAIN, "my-client", updateClient).test();
        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();

        verify(clientRepository, times(1)).findById(anyString());
        verify(clientRepository, times(1)).update(any(Client.class));
    }

    @Test
    public void shouldUpdate3_technicalException() {
        UpdateClient updateClient = Mockito.mock(UpdateClient.class);
        when(updateClient.getIdentities()).thenReturn(new HashSet<>(Arrays.asList("id1", "id2")));
        when(clientRepository.findById("my-client")).thenReturn(Maybe.just(new Client()));
        when(identityProviderService.findById(anyString())).thenReturn(Maybe.error(TechnicalException::new));

        TestObserver testObserver = clientService.update(DOMAIN, "my-client", updateClient).test();
        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();

        verify(clientRepository, times(1)).findById(anyString());
        verify(clientRepository, never()).update(any(Client.class));
    }

    @Test
    public void shouldUpdate_clientNotFound() {
        UpdateClient updateClient = Mockito.mock(UpdateClient.class);
        when(clientRepository.findById("my-client")).thenReturn(Maybe.empty());

        TestObserver testObserver = clientService.update(DOMAIN, "my-client", updateClient).test();

        testObserver.assertError(ClientNotFoundException.class);
        testObserver.assertNotComplete();

        verify(clientRepository, times(1)).findById(anyString());
        verify(clientRepository, never()).update(any(Client.class));
    }

    @Test
    public void shouldDelete() {
        Client existingClient = Mockito.mock(Client.class);
        when(existingClient.getDomain()).thenReturn("my-domain");
        when(clientRepository.findById("my-client")).thenReturn(Maybe.just(existingClient));
        when(clientRepository.delete("my-client")).thenReturn(Completable.complete());
        when(domainService.reload(eq("my-domain"), any())).thenReturn(Single.just(new Domain()));

        TestObserver testObserver = clientService.delete("my-client").test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(clientRepository, times(1)).delete("my-client");
    }

    @Test
    public void shouldDelete_technicalException() {
        when(clientRepository.findById("my-client")).thenReturn(Maybe.just(new Client()));
        when(clientRepository.delete(anyString())).thenReturn(Completable.error(TechnicalException::new));

        TestObserver testObserver = clientService.delete("my-client").test();
        testObserver.awaitTerminalEvent();

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldDelete2_technicalException() {
        when(clientRepository.findById("my-client")).thenReturn(Maybe.error(TechnicalException::new));

        TestObserver testObserver = clientService.delete("my-client").test();
        testObserver.awaitTerminalEvent();

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldDelete_clientNotFound() {
        when(clientRepository.findById("my-client")).thenReturn(Maybe.empty());

        TestObserver testObserver = clientService.delete("my-client").test();
        testObserver.awaitTerminalEvent();

        testObserver.assertError(ClientNotFoundException.class);
        testObserver.assertNotComplete();

        verify(clientRepository, never()).delete("my-client");
    }
}
