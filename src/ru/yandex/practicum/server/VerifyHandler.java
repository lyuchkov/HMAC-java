package ru.yandex.practicum.server;

import ru.yandex.practicum.config.AppConfig;
import ru.yandex.practicum.model.requests.VerifyRequest;
import ru.yandex.practicum.model.responses.VerifyResponse;
import ru.yandex.practicum.service.HmacService;

public class VerifyHandler extends BasePostRequestHandler<VerifyRequest> {
    private final HmacService hmacService;

    public VerifyHandler(HmacService hmacService, AppConfig config) {
        super(VerifyRequest.class, config);
        this.hmacService = hmacService;
    }

    @Override
    protected Object process(VerifyRequest request) throws ApiException {
        String msg = request.getMsg();
        String signature = request.getSignature();


        if (msg == null) {
            throw new ApiException(400, "invalid_msg");
        }

        if (msg.length() > config.getMaxMsgSizeBytes()) {
            throw new ApiException(413, "payload_too_large");
        }

        if (signature == null) {
            throw new ApiException(400, "invalid_signature_format");
        }

        if (signature.length() > config.getMaxMsgSizeBytes()) {
            throw new ApiException(413, "payload_too_large");
        }

        try {
            boolean isValid = hmacService.verify(msg, signature);
            return new VerifyResponse(isValid);
        } catch (IllegalArgumentException e) {
            throw new ApiException(400, "invalid_signature_format");
        }
    }
}