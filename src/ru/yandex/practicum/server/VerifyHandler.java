package ru.yandex.practicum.server;

import ru.yandex.practicum.config.AppConfig;
import ru.yandex.practicum.model.requests.VerifyRequest;
import ru.yandex.practicum.model.responses.VerifyResponse;
import ru.yandex.practicum.service.HmacService;

import java.util.logging.Logger;

public class VerifyHandler extends BasePostRequestHandler<VerifyRequest> {
    private static final Logger logger = Logger.getLogger(VerifyHandler.class.getName());
    private final HmacService hmacService;

    public VerifyHandler(HmacService hmacService, AppConfig config) {
        super(VerifyRequest.class, config);
        this.hmacService = hmacService;
    }

    @Override
    protected Object process(VerifyRequest request) throws ApiException {
        logger.info("Received /verify request");

        String msg = request.getMsg();

        if (msg == null) {
            logger.warning("Verify failed: missing msg");
            throw new ApiException(400, "invalid_msg");
        }

        if (msg.length() > config.getMaxMsgSizeBytes()) {
            logger.warning("Verify failed: msg bigger than " + config.getMaxMsgSizeBytes() + " symbols");
            throw new ApiException(413, "payload_too_large");
        }

        String signature = request.getSignature();

        if (signature == null) {
            logger.warning("Verify failed: missing signature");
            throw new ApiException(400, "invalid_signature_format");
        }

        if (signature.length() > config.getMaxMsgSizeBytes()) {
            logger.warning("Verify failed: signature bigger than " + config.getMaxMsgSizeBytes() + " symbols");
            throw new ApiException(413, "payload_too_large");
        }

        try {
            boolean isValid = hmacService.verify(msg, signature);
            logger.info("Verification completed. Result: " + isValid);
            return new VerifyResponse(isValid);
        } catch (IllegalArgumentException e) {
            logger.warning("Verify failed: invalid signature format");
            throw new ApiException(400, "invalid_signature_format");
        }
    }
}