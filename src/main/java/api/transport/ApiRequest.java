package api.transport;

import api.endpoint.Endpoint;

import java.util.List;
import java.util.Map;

public record ApiRequest<RES>(
        Endpoint<RES> endpoint,
        Map<String, ?> pathParams,
        Map<String, ?> queryParams,
        List<MultipartPart> multipartParts,
        Object body
) {
    public static <RES> ApiRequest<RES> withBody(
            Endpoint<RES> endpoint,
            Object body
    ) {
        return new ApiRequest<>(
                endpoint,
                Map.of(),
                Map.of(),
                List.of(),
                body
        );
    }

    public static <RES> ApiRequest<RES> withoutBody(Endpoint<RES> endpoint) {
        return new ApiRequest<>(
                endpoint,
                Map.of(),
                Map.of(),
                List.of(),
                null
        );
    }

    public static <RES> ApiRequest<RES> withPathParams(
            Endpoint<RES> endpoint,
            Map<String, ?> pathParams
    ) {
        return new ApiRequest<>(
                endpoint,
                pathParams,
                Map.of(),
                List.of(),
                null
        );
    }

    public static <RES> ApiRequest<RES> withBodyAndPathParams(
            Endpoint<RES> endpoint,
            Map<String, ?> pathParams,
            Object body
    ) {
        return new ApiRequest<>(
                endpoint,
                pathParams,
                Map.of(),
                List.of(),
                body
        );
    }

    public static <RES> ApiRequest<RES> withQueryParams(
            Endpoint<RES> endpoint,
            Map<String, ?> queryParams
    ) {
        return new ApiRequest<>(endpoint, Map.of(), queryParams, List.of(),null);
    }

    public static <RES> ApiRequest<RES> withPathParamsAndMultipart(
            Endpoint<RES> endpoint,
            Map<String, ?> pathParams,
            List<MultipartPart> multipartParts
    ) {
        return new ApiRequest<>(
                endpoint,
                pathParams,
                Map.of(),
                multipartParts,
                null
        );
    }
}
