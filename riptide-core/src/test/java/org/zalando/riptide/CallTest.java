package org.zalando.riptide;

/*
 * ⁣​
 * Riptide
 * ⁣⁣
 * Copyright (C) 2015 Zalando SE
 * ⁣⁣
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ​⁣
 */

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.web.client.MockRestServiceServer;
import org.zalando.riptide.model.AccountBody;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CompletionException;

import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.zalando.riptide.Bindings.anyStatus;
import static org.zalando.riptide.Bindings.on;
import static org.zalando.riptide.Navigators.status;
import static org.zalando.riptide.Route.responseEntityOf;

public final class CallTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    private final URI url = URI.create("https://api.example.com/accounts/123");

    private final Rest unit;
    private final MockRestServiceServer server;

    public CallTest() {
        final MockSetup setup = new MockSetup("https://api.example.com/");
        this.unit = setup.getRest();
        this.server = setup.getServer();
    }

    @Test
    public void shouldCallEntity() throws Exception {
        server.expect(requestTo(url)).andRespond(
                withSuccess()
                        .body(new ClassPathResource("account.json"))
                        .contentType(APPLICATION_JSON));

        @SuppressWarnings("unchecked")
        final ThrowingConsumer<AccountBody> verifier = mock(ThrowingConsumer.class);

        unit.get("accounts/123")
                .dispatch(status(),
                        on(OK).call(AccountBody.class, verifier),
                        anyStatus().call(this::fail));

        verify(verifier).accept(any(AccountBody.class));
    }

    @Test
    public void shouldCallResponseEntity() throws Exception {
        server.expect(requestTo(url)).andRespond(
                withSuccess()
                        .body(new ClassPathResource("account.json"))
                        .contentType(APPLICATION_JSON));

        @SuppressWarnings("unchecked")
        final ThrowingConsumer<ResponseEntity<AccountBody>> verifier = mock(ThrowingConsumer.class);

        unit.get("accounts/123")
                .dispatch(status(),
                        on(OK).call(responseEntityOf(AccountBody.class), verifier),
                        anyStatus().call(this::fail));

        @SuppressWarnings("unchecked")
        final ArgumentCaptor<ResponseEntity<AccountBody>> captor = ArgumentCaptor.forClass(ResponseEntity.class);
        verify(verifier).accept(captor.capture());
        final ResponseEntity<AccountBody> entity = captor.getValue();

        assertThat(entity.getStatusCode(), is(OK));
        assertThat(entity.getHeaders(), is(not(anEmptyMap())));
        assertThat(entity.getBody().getName(), is("Acme Corporation"));
    }

    @Test
    public void shouldCallWithoutParameters() throws Exception {
        server.expect(requestTo(url)).andRespond(
                withSuccess()
                        .body(new ClassPathResource("account.json"))
                        .contentType(APPLICATION_JSON));

        final ThrowingRunnable verifier = mock(ThrowingRunnable.class);

        unit.get("accounts/123")
                .dispatch(status(),
                        on(OK).call(verifier),
                        anyStatus().call(this::fail));

        verify(verifier).run();
    }

    @Test
    public void shouldThrowCheckedExceptionOnEntity() {
        server.expect(requestTo(url)).andRespond(
                withSuccess()
                        .body(new ClassPathResource("account.json"))
                        .contentType(APPLICATION_JSON));

        exception.expect(CompletionException.class);
        exception.expectCause(instanceOf(IOException.class));

        unit.get("accounts/123")
                .dispatch(status(),
                        on(OK).call(AccountBody.class, this::validateEntity),
                        anyStatus().call(this::fail))
                .join();
    }

    private void validateEntity(final AccountBody account) throws IOException {
        throw new IOException();
    }

    private void fail(final ClientHttpResponse response) throws IOException {
        throw new AssertionError(response.getRawStatusCode());
    }

}