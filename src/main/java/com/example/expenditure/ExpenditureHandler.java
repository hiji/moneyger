package com.example.expenditure;

import com.example.error.ErrorResponseBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;

import static org.springframework.http.HttpStatus.NOT_FOUND;

public class ExpenditureHandler {

    private final ExpenditureRepository expenditureRepository;

    public ExpenditureHandler(ExpenditureRepository expenditureRepository) {
        this.expenditureRepository = expenditureRepository;
    }

    public RouterFunction<ServerResponse> routes() {
        return RouterFunctions.route()
                // TODO Routingの定義
                // GET /expenditures
                .GET("/expenditures", this::list)
                // POST /expenditures
                .POST("/expenditures", this::post)
                // GET /expenditures/{expenditureId}
                .GET("/expenditures/{expenditureId}", this::get)
                .DELETE("/expenditures/{expenditureId}", this::delete)
                .build();
    }

    Mono<ServerResponse> list(ServerRequest req) {
        return ServerResponse.ok().body(this.expenditureRepository.findAll(), Expenditure.class);
    }

    Mono<ServerResponse> post(ServerRequest req) {
        return req.bodyToMono(Expenditure.class)
                // TODO
                // ExpenditureRepositoryでExpenditureを保存
                // Hint: ExpenditureRepository.saveを使ってください。
//                .flatMap(this.expenditureRepository::save)
//                .flatMap(created -> ServerResponse
//                        .created(UriComponentsBuilder.fromUri(req.uri()).path("/{expenditureId}").build(created.getExpenditureId()))
//                        .bodyValue(created));
                .flatMap(expenditure -> expenditure.validate()
                .bimap(v -> new ErrorResponseBuilder().withStatus(HttpStatus.BAD_REQUEST).withDetails(v).build(), this.expenditureRepository::save)
                        .fold(error -> ServerResponse.badRequest().bodyValue(error),
                                result -> result.flatMap(created -> ServerResponse
                                        .created(UriComponentsBuilder.fromUri(req.uri()).path("/{expenditureId}").build(created.getExpenditureId()))
                                        .bodyValue(created))));
    }

    Mono<ServerResponse> get(ServerRequest req) {
        return this.expenditureRepository.findById(Integer.valueOf(req.pathVariable("expenditureId")))
                .flatMap(expenditure -> ServerResponse.ok().bodyValue(expenditure))
                .switchIfEmpty(Mono.defer(() -> ServerResponse
                        .status(NOT_FOUND)
//                        .bodyValue(new LinkedHashMap<String, Object>() {
////                                       {
////                                           put("status", 404);
////                                           put("error", "Not Found");
////                                           put("message", "The given expenditure is not found.");
////                                       }
////                                   })))
                        .bodyValue(new ErrorResponseBuilder()
                                .withMessage("The given expenditure is not found.")
                                .withStatus(NOT_FOUND)
                                .build())))
                // TODO
                // expenditureが存在しない場合は404を返す。エラーレスポンスは{"status":404,"error":"Not Found","message":"The given expenditure is not found."}
                // Hint: switchIfEmptyおよびServerResponse.statusを使ってください。
                ;
    }

    Mono<ServerResponse> delete(ServerRequest req) {
        return ServerResponse.noContent()
                .build(this.expenditureRepository.deleteById(Integer.valueOf(req.pathVariable("expenditureId"))));
    }
}