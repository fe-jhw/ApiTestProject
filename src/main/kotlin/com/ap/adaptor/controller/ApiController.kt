package com.ap.adaptor.controller

import com.ap.adaptor.constants.Constants
import com.ap.adaptor.entity.*
import com.ap.adaptor.service.AdaptorService
import com.ap.adaptor.service.CombinationApiService
import com.ap.adaptor.service.PerformService
import com.ap.adaptor.utils.logger
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

@Tag(name = "테스트", description = "API 테스트")
@RestController
class ApiController(
    val adaptorService: AdaptorService,
    val combinationApiService: CombinationApiService,
    val performService: PerformService
) {

    val log = logger()

    @Operation(summary = "API 요청", description = "HTTP Method에 해당하는 API 요청")
    @PostMapping("/api/call")
    suspend fun callApi(@RequestBody requestData: RequestData):MutableList<ResponseData>{
        log.info("Api Call Request : $requestData")
        return adaptorService.responses(requestData)
    }

    @Operation(summary = "API 성능 테스트", description = "API 성능 테스트")
    @PostMapping("/api/perform")
    suspend fun performApi(@RequestBody requestDataList: RequestDataList): ResponseDataList{
        log.info("Api Performance : $requestDataList")
        return performService.measurePerform(requestDataList)
    }

    @Operation(summary = "COMBINE", description = "API 조합 요청")
    @PostMapping("/api/combine")
    suspend fun callCombineApi(@RequestBody combination: Combination){
        log.info("Call COMBINE Request : $combination")
        combinationApiService.combineApis(combination)

    }

    @PostMapping("/api/test")
    suspend fun testApi():MutableMap<String, Any>{
        return performService.measureTestPerform()
    }
}