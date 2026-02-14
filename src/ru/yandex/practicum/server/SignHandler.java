package ru.yandex.practicum.server;

import ru.yandex.practicum.config.AppConfig;
import ru.yandex.practicum.model.requests.SignRequest;
import ru.yandex.practicum.model.responses.SignResponse;
import ru.yandex.practicum.service.HmacService;

import java.util.logging.Logger;

public class SignHandler extends BasePostRequestHandler<SignRequest> {
    private static final Logger logger = Logger.getLogger(SignHandler.class.getName());
    private final HmacService hmacService;

    public SignHandler(HmacService hmacService, AppConfig config) {
        super(SignRequest.class, config);
        this.hmacService = hmacService;
    }

    @Override
    protected Object process(SignRequest request) throws ApiException {
        String msg = request.getMsg();

        if (msg == null || msg.isEmpty()) {
            logger.warning("Sign failed: msg is empty or null");
            throw new ApiException(400, "invalid_msg");
        }
        if (msg.length() > config.getMaxMsgSizeBytes()) {
            logger.warning("Sign failed: message size " + msg.length() + " exceeds limit");
            throw new ApiException(413, "payload_too_large");
        }

        String signature = hmacService.sign(msg);

        logger.info("Message signed successfully");
        return new SignResponse(signature);
    }
}