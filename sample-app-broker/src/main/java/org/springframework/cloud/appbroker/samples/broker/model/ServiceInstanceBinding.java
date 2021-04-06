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

package org.springframework.cloud.appbroker.samples.broker.model;

import org.springframework.cloud.servicebroker.model.instance.OperationState;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

public class ServiceInstanceBinding {

	@Id
	private Long id;

	private String bindingId;

	private String serviceInstanceId;

	private String description;

	private OperationState operationState;

	private LocalDateTime lastUpdated;

	public ServiceInstanceBinding() {
	}

	public ServiceInstanceBinding(Long id, String bindingId, String serviceInstanceId, String description, OperationState operationState, LocalDateTime lastUpdated) {
		this.id = id;
		this.bindingId = bindingId;
		this.serviceInstanceId = serviceInstanceId;
		this.description = description;
		this.operationState = operationState;
		this.lastUpdated = lastUpdated;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getBindingId() {
		return bindingId;
	}

	public void setBindingId(String bindingId) {
		this.bindingId = bindingId;
	}

	public String getServiceInstanceId() {
		return serviceInstanceId;
	}

	public void setServiceInstanceId(String serviceInstanceId) {
		this.serviceInstanceId = serviceInstanceId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public OperationState getOperationState() {
		return operationState;
	}

	public void setOperationState(OperationState operationState) {
		this.operationState = operationState;
	}

	public LocalDateTime getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(LocalDateTime lastUpdated) {
		this.lastUpdated = lastUpdated;
	}
}
