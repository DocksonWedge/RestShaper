package org.shaper.swagger.model

import io.swagger.models.Method

data class EndpointSpec(val url:String, val path: String, val verb:Method)