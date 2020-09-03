package org.shaper.tester

import org.shaper.swagger.model.EndpointSpec

object TestRunner{
    //TODO shrink Any return type of output once we have a better idea what it looks like
    fun shapeEndpoint(endpoint: EndpointSpec, output: (EndpointSpec) -> Any){
        endpoint.params
    }
}
