/*
 * Copyright 2002-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.appbroker.samples.broker.repository;

import org.springframework.cloud.appbroker.samples.broker.model.ServiceInstance;
import reactor.core.publisher.Mono;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ServiceInstanceRepository extends ReactiveCrudRepository<ServiceInstance, Long> {

	@Query("select * from service_instance where service_instance_id = :service_instance_id")
	Mono<ServiceInstance> findByServiceInstanceId(@Param("service_instance_id") String serviceInstanceId);

	@Query("delete from service_instance where service_instance_id = :service_instance_id")
	Mono<Void> deleteByServiceInstanceId(@Param("service_instance_id") String serviceInstanceId);

}
