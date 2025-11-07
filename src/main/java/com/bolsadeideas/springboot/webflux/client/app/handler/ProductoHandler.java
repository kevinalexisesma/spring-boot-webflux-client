package com.bolsadeideas.springboot.webflux.client.app.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.bolsadeideas.springboot.webflux.client.app.models.Producto;
import com.bolsadeideas.springboot.webflux.client.app.models.services.ProductoService;

import static org.springframework.web.reactive.function.BodyInserters.*;
import static org.springframework.http.MediaType.*;

import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import reactor.core.publisher.Mono;

@Component
public class ProductoHandler {

    @Autowired
    private ProductoService service;

    public Mono<ServerResponse> listar(ServerRequest request) {
        return ServerResponse.ok()
                .contentType(APPLICATION_JSON)
                .body(service.findAll(), Producto.class);
    }

    public Mono<ServerResponse> ver(ServerRequest request) {
        String id = request.pathVariable("id");
        return errorHandler(
                service.findById(id)
                        .flatMap(producto -> ServerResponse.ok()
                                .contentType(APPLICATION_JSON)
                                .body(fromValue(producto)))
                        .switchIfEmpty(ServerResponse.notFound().build()));
    }

    public Mono<ServerResponse> crear(ServerRequest request) {
        Mono<Producto> producto = request.bodyToMono(Producto.class);
        return producto
                .flatMap(p -> {
                    if (p.getCreateAt() == null) {
                        p.setCreateAt(new Date());
                    }
                    return service.save(p);
                })
                .flatMap(p -> ServerResponse.created(URI.create("/api/client/".concat(p.getId())))
                        .contentType(APPLICATION_JSON)
                        .bodyValue(p))
                .onErrorResume(WebClientResponseException.class, errorResponse -> {
                    if (errorResponse.getStatusCode() == HttpStatus.BAD_REQUEST) {
                        return ServerResponse.badRequest()
                                .contentType(APPLICATION_JSON)
                                .bodyValue(errorResponse.getResponseBodyAsString());
                    }
                    return Mono.error(errorResponse);
                });
    }

    public Mono<ServerResponse> editar(ServerRequest request) {
        String id = request.pathVariable("id");
        Mono<Producto> producto = request.bodyToMono(Producto.class);
        return errorHandler(
                producto
                        .flatMap(p -> service.update(p, id))
                        .flatMap(p -> ServerResponse.created(URI.create("/api/client/".concat(id)))
                                .contentType(APPLICATION_JSON)
                                .bodyValue(p)));
    }

    public Mono<ServerResponse> eliminar(ServerRequest request) {
        String id = request.pathVariable("id");
        return errorHandler(
                service.delete(id)
                        .then(ServerResponse.noContent().build()));
    }

    public Mono<ServerResponse> upload(ServerRequest request) {
        String id = request.pathVariable("id");
        return errorHandler(
                request.multipartData()
                        .map(multipart -> multipart.toSingleValueMap().get("file"))
                        .cast(FilePart.class)
                        .flatMap(file -> service.upload(file, id))
                        .flatMap(producto -> ServerResponse.created(URI.create("/api/client/".concat(id)))
                                .contentType(APPLICATION_JSON)
                                .bodyValue(producto)));
    }

    private Mono<ServerResponse> errorHandler(Mono<ServerResponse> request) {
        return request.onErrorResume(WebClientResponseException.class, errorResponse -> {
            if (errorResponse.getStatusCode() == HttpStatus.NOT_FOUND) {
                Map<String, Object> body = new HashMap<>();
                body.put("error", "No existe el producto: ".concat(errorResponse.getMessage()));
                body.put("timestamp", new Date());
                body.put("status", errorResponse.getStatusCode().value());
                return ServerResponse
                        .status(HttpStatus.NOT_FOUND)
                        .bodyValue(body);
            }
            return Mono.error(errorResponse);
        });
    }
}
