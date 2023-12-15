package com.ap.adaptor.handler

import com.ap.adaptor.dto.*
import com.ap.adaptor.service.AdaptorService
import com.ap.adaptor.utils.logger
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.*
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import kotlin.IllegalArgumentException
import kotlin.system.measureTimeMillis


@Component
class WebSocketHandler(
    val adaptorService: AdaptorService
) : WebSocketHandler {

    val log = logger()

    override fun handle(session: WebSocketSession): Mono<Void> {

        val receiveAndCallApi = session.receive()
            .flatMap { message ->
                val requestData = message.payloadAsText
                val coroutineScope = CoroutineScope(Dispatchers.IO)

                coroutineScope.launch {
                    val performData = parseRequestData(requestData)
                    if(performData != null){
                        repeat(performData.repeatCount) {
                            callApi(performData, session)
                            delay(performData.interval)
                        }
                    }

                    session.send(Mono.just(session.textMessage("connection close success")))
                        .then(session.close())
                        .subscribe()

                }
                Mono.empty<Void>()

            }
            .doOnError { error ->
                session.send(Mono.just(session.textMessage("connection close : $error")))
                    .then(session.close())
                    .subscribe()
            }
            .then()


        return session.send(Mono.just(session.textMessage("Connection open success")))
            .thenMany(receiveAndCallApi)
            .then()

    }

    private suspend fun callApi(performData: PerformData, session: WebSocketSession) {
        coroutineScope {
            val userCallApiTime = measureTimeMillis{
                val deferredValue = List(performData.userCount) {
                    async { adaptorService.responsesForPerForm(performData.requestDataList, session, it) }
                }
                val totalResponseWithTime = deferredValue.awaitAll()
//                totalResponseWithTime.forEach{session.send(Mono.just(session.textMessage(it.toString()))).subscribe()}
            }

            log.info("User ${performData.userCount} finish API Call Total Time : $userCallApiTime")
        }
    }

    private suspend fun parseRequestData(payload: String): PerformData? = withContext(Dispatchers.IO){
        try{
            if(payload.isNotBlank()){
                val objectMapper = jacksonObjectMapper()
                val map = objectMapper.readValue(payload, MutableMap::class.java)

                val performData: PerformData = objectMapper.convertValue(map, PerformData::class.java)
                performData

            }else{
                throw IllegalArgumentException("Input string cannot be blank.")
            }
        }catch (e: Exception){
            throw IllegalArgumentException(e)
        }
    }


}