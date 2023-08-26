package com.yao.yaoApiGateway;

/**
 * @author DH
 * @version 1.0
 * @description 接口调用的网关验证
 * @date 2023/5/11 16:55
 */

import com.yao.common.model.entity.InterfaceInfo;
import com.yao.common.model.entity.User;
import com.yao.common.service.InnerInterfaceInfoService;
import com.yao.common.service.InnerUserInterfaceInfoService;
import com.yao.common.service.InnerUserService;
import com.yao.yaoapiclientsdk.utils.SignUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class CustomGlobalFilter implements GlobalFilter, Ordered {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @DubboReference
    private InnerUserInterfaceInfoService innerUserInterfaceInfoService;
    @DubboReference
    private InnerUserService innerUserService;
    @DubboReference
    private InnerInterfaceInfoService innerInterfaceInfoService;
    public final List<String> IP_WHITE_LIST = new ArrayList<>();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        /*
        1.请求日志
        2.黑白名单
        3.用户鉴权(API签名认证)
        4.随机数配合时间戳：防重放
        5.接口参数是否正确
        6.判断调用次数
        7.获取响应结果，打上响应日志
        8.接口调用次数+1
        */
        // 用户发送请求到API网关
        //1. 请求日志
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().toString();
        String sourceAddress = request.getLocalAddress().getHostString();

        log.info("请求唯一标识：" + request.getId());
        log.info("请求路径：" + path);
        log.info("请求方法：" + request.getMethod());
        log.info("请求参数：" + request.getQueryParams());
        log.info("请求来源地址：" + sourceAddress);

        //2. 黑白名单(暂未实现）
        IP_WHITE_LIST.add(sourceAddress);
        ServerHttpResponse response = exchange.getResponse();
        if (!IP_WHITE_LIST.contains(sourceAddress)) {
            response.setStatusCode(HttpStatus.FORBIDDEN);
            return response.setComplete();
        }

        //3. 用户鉴权（判断ak、sk是否合法）
        HttpHeaders headers = request.getHeaders();
        String accessKey = headers.getFirst("accessKey");
        //传入的参数
        String bodyOrigin = headers.getFirst("body");
        String body = null;
        try {
            body = URLDecoder.decode(bodyOrigin, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        log.info("请求体："+body);
        //查询数据库判断是否发配给用户密钥
        User user = innerUserService.getInvokeUser(accessKey);
        if (user == null) {
            return handleInvokeError(response);
        }
        //密钥鉴权
        Long userId = user.getId();
        if (accessKey != null && !accessKey.equals(user.getAccessKey())) {
            return handleNoAuth(response);
        }
        //sign:把密钥和传入的值加密成一个签名
        String sign = headers.getFirst("sign");
        //AK/SK鉴权
        String secretKey = user.getSecretKey();
        String serverSign = SignUtils.getSign(body, accessKey, secretKey);
        if (sign != null && !sign.equals(serverSign)) {
            return handleNoAuth(response);
        }

        //4、随机数配合时间戳：防重放
        //时间戳
        String timestamp = headers.getFirst("timestamp");
        //时间和当前时间应不超过10分钟
        long currentTime = System.currentTimeMillis() / 1000;
        if (timestamp != null && currentTime - Long.parseLong(timestamp) > 60 * 5) {
            return handleNoAuth(response);
        }
        String nonce = headers.getFirst("nonce");
        Boolean aBoolean = stringRedisTemplate.opsForValue().setIfAbsent(nonce, "ok", 10, TimeUnit.MINUTES);
        if (Boolean.FALSE.equals(aBoolean)) {
            return handleNoAuth(response);
        }


        //5. 请求的模拟接口是否存在
        //从数据库查询接口是否存在，一起请求方法是否匹配
        InterfaceInfo interfaceInfo = innerInterfaceInfoService.getInterfaceInfo("http://localhost:8888" + path, String.valueOf(request.getMethod()));
        if (interfaceInfo == null) {
            //404
            return handleInvokeNotFound(response);
        }
        Long interfaceInfoId = interfaceInfo.getId();


        //6.该用户是否还有调用次数
        boolean times = innerUserInterfaceInfoService.callTimes(interfaceInfoId, userId);
        //次数不足抛403，注意对接
        if (!times) {
            return handleInvokeForbidden(response);
        }
        /*  请求转发，调用模拟接口
        Mono<Void> filter = chain.filter(exchange);*/
        //7. 响应日志
        log.info("custom global filter");
        return handleResponse(exchange, chain, interfaceInfoId, userId);
    }

    public Mono<Void> handleResponse(ServerWebExchange exchange, GatewayFilterChain chain, long interfaceInfoId, long userId) {
        try {
            ServerHttpResponse originalResponse = exchange.getResponse();
            //缓冲数据
            DataBufferFactory bufferFactory = originalResponse.bufferFactory();
            //拿到响应码
            HttpStatus statusCode = originalResponse.getStatusCode();
            log.info("status:{}", statusCode);
            if (statusCode == HttpStatus.OK) {
                //装饰，增强能力
                ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
                    //等调用完转发的接口后才会执行
                    @Override
                    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                        //log.info("body instanceof Flux: {}", (body instanceof Flux));
                        if (body instanceof Flux) {
                            Flux<? extends DataBuffer> fluxBody = Flux.from(body);
                            //往返回值里写数据
                            return super.writeWith(fluxBody.map(dataBuffer -> {
                                //8.调用成功，接口调用次数+1
                                log.info("interfaceInfoId:{}", interfaceInfoId);
                                log.info("userId:{}", userId);
                                try {
                                    innerUserInterfaceInfoService.invokeCount(interfaceInfoId, userId);
                                } catch (Exception e) {
                                    log.info("error:{}", e);
                                }
                                byte[] content = new byte[dataBuffer.readableByteCount()];
                                dataBuffer.read(content);
                                DataBufferUtils.release(dataBuffer);//释放掉内存
                                // 构建日志
                                StringBuilder sb2 = new StringBuilder(200);
                                sb2.append("<--- {} {} \n");
                                List<Object> rspArgs = new ArrayList<>();
                                rspArgs.add(originalResponse.getStatusCode());
                                String data = new String(content, StandardCharsets.UTF_8);//data
                                sb2.append(data);
                                log.info(sb2.toString(), rspArgs.toArray());//log.info("<-- {} {}\n", originalResponse.getStatusCode(), data);
                                log.info("响应数据：" + data);
                                return bufferFactory.wrap(content);
                            }));
                        } else {
                            //9. 调用失败，返回一个规范的错误码
                            log.error("<--- {} 响应code异常", getStatusCode());
                        }
                        return super.writeWith(body);
                    }
                };
                return chain.filter(exchange.mutate().response(decoratedResponse).build());
            }
            return chain.filter(exchange);//降级处理返回数据
        } catch (Exception e) {
            log.error("gateway log exception.\n" + e);
            return chain.filter(exchange);
        }

    }

    @Override
    public int getOrder() {
        return -1;
    }

    /**
     * 异常定义 （注意和调用方法的主后端对应好）
     *
     * @param response
     * @return
     */

    public Mono<Void> handleNoAuth(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.BAD_REQUEST); //400
        return response.setComplete();
    }

    public Mono<Void> handleInvokeError(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.METHOD_NOT_ALLOWED);
        return response.setComplete();
    }

    public Mono<Void> handleInvokeForbidden(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.FORBIDDEN);
        return response.setComplete();
    }

    /**
     * 调用接口未找到
     *
     * @param response
     * @return
     */
    public Mono<Void> handleInvokeNotFound(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.NOT_FOUND);
        return response.setComplete();
    }
}
