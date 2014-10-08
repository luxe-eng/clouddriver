/*
 * Copyright 2014 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.spinnaker.kato.deploy.gce.ops

import com.google.api.services.replicapool.ReplicapoolScopes
import com.netflix.spinnaker.kato.data.task.Task
import com.netflix.spinnaker.kato.data.task.TaskRepository
import com.netflix.spinnaker.kato.deploy.gce.description.ResizeGoogleReplicaPoolDescription
import com.netflix.spinnaker.kato.orchestration.AtomicOperation

class ResizeGoogleReplicaPoolAtomicOperation implements AtomicOperation<Void> {
  // TODO(duftler): This should move to a common location.
  private static final String APPLICATION_NAME = "Spinnaker"
  private static final String BASE_PHASE = "RESIZE_REPLICA_POOL"

  private static Task getTask() {
    TaskRepository.threadLocalTask.get()
  }

  private final ResizeGoogleReplicaPoolDescription description
  private final ReplicaPoolBuilder replicaPoolBuilder

  ResizeGoogleReplicaPoolAtomicOperation(ResizeGoogleReplicaPoolDescription description,
                                         ReplicaPoolBuilder replicaPoolBuilder) {
    this.description = description
    this.replicaPoolBuilder = replicaPoolBuilder
  }

  @Override
  Void operate(List priorOutputs) {
    task.updateStatus BASE_PHASE, "Initializing resize of replica pool $description.replicaPoolName in $description.zone..."

    def project = description.credentials.project

    def credentialBuilder = description.credentials.createCredentialBuilder(ReplicapoolScopes.REPLICAPOOL)

    def replicapool = replicaPoolBuilder.buildReplicaPool(credentialBuilder, APPLICATION_NAME);

    replicapool.pools().resize(project,
                               description.zone,
                               description.replicaPoolName).setNumReplicas(description.numReplicas).execute()

    task.updateStatus BASE_PHASE, "Done resizing replica pool $description.replicaPoolName in $description.zone."
    null
  }
}