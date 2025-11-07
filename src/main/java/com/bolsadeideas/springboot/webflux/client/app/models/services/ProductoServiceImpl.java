package com.bolsadeideas.springboot.webflux.client.app.models.services;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.bolsadeideas.springboot.webflux.client.app.models.Producto;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ProductoServiceImpl implements ProductoService {

    @Autowired
    private WebClient client;

    @Override
    public Flux<Producto> findAll() {
        return client.get().accept(MediaType.APPLICATION_JSON)
                .exchangeToFlux(response -> response.bodyToFlux(Producto.class));
    }

    @Override
    public Mono<Producto> findById(String id) {
        return client.get().uri("/{id}", Collections.singletonMap("id", id))
                .accept(MediaType.APPLICATION_JSON)
                .exchangeToMono(response -> response.bodyToMono(Producto.class));
    }

    @Override
    public Mono<Producto> save(Producto producto) {
        return client.post()
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(producto))
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToMono(Producto.class);
                    } else if (response.statusCode().equals(HttpStatus.BAD_REQUEST)) {
                        return response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new WebClientResponseException(
                                        "Error 400: " + body,
                                        400,
                                        "Bad Request",
                                        null,
                                        body.getBytes(),
                                        null)));
                    } else {
                        return response.createException().flatMap(Mono::error);
                    }
                });
    }

    @Override
    public Mono<Producto> update(Producto producto, String id) {
        return client.put().uri("/{id}", Collections.singletonMap("id", id))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(producto))
                .exchangeToMono(response -> response.bodyToMono(Producto.class));
    }

    @Override
    public Mono<Void> delete(String id) {
        return client.delete().uri("/{id}", Collections.singletonMap("id", id))
                .exchangeToMono(t -> t.bodyToMono(Void.class));
    }

    @Override
    public Mono<Producto> upload(FilePart file, String id) {
        MultipartBodyBuilder parts = new MultipartBodyBuilder();
        parts
                .asyncPart("file", file.content(), DataBuffer.class)
                .headers(h -> {
                    h.setContentDispositionFormData("file", file.filename());
                });
        return client.post().uri("/upload/{id}", Collections.singletonMap("id", id))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(parts.build())
                .retrieve()
                .bodyToMono(Producto.class);
    }

}
