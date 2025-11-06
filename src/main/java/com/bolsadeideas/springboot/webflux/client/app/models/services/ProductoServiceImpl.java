package com.bolsadeideas.springboot.webflux.client.app.models.services;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

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
                .exchangeToMono(response -> response.bodyToMono(Producto.class));
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

}
