package com.example.leader.demo;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.kubernetes.integration.tests.commons.Commons;
import org.springframework.cloud.kubernetes.integration.tests.commons.fabric8_client.Util;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.k3s.K3sContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.io.File;
import java.io.InputStream;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class DemoApplicationIT {

	private static final Logger LOG = LoggerFactory.getLogger(DemoApplicationIT.class);

	private static final String IMAGE_NAME = "nontster/spring-leader" + ":" + Commons.pomVersion();

	private static final String NAMESPACE = "default";

	// Declare the container without initializing it.
	private static K3sContainer K3S;

	private static Util util;

	private static KubernetesClient client;

	@BeforeAll
	static void beforeAll() throws Exception {
		// Get the Docker host IP that Testcontainers will use.
		String dockerHostIp = DockerClientFactory.instance().dockerHostIpAddress();

		// Initialize the container, adding the docker host IP as a TLS Subject Alternative Name (SAN).
		K3S = new K3sContainer(DockerImageName.parse("rancher/k3s:v1.33.3-k3s1"))
				.withPrivilegedMode(true)
				.withCommand("server",
						"--disable=traefik",
						"--disable=servicelb",
						"--disable=metrics-server",
						"--tls-san=" + dockerHostIp
				);

		K3S.withCopyFileToContainer(
				MountableFile.forHostPath(new File("target/spring-leader-1.0.1.tar.gz").toPath()),
				"/tmp/spring-leader.tar.gz"
		);
		K3S.start();
		K3S.execInContainer("k3s", "ctr", "images", "import", "/tmp/spring-leader.tar.gz");

		util = new Util(K3S);
		client = util.client();
		util.setUp(NAMESPACE);

		//deployManifests();
	}

	@AfterAll
	static void afterAll() throws Exception {
		String logs = K3S.getLogs();
		LOG.info(logs);

		// Delete Kubernetes manifests from classpath
		deleteManifests();

		if (K3S != null) {
			K3S.stop();
		}
	}

	@Test
	void testLeaderElection() {
		Awaitility.await().atMost(Duration.ofMinutes(2)).until(() -> {
			ConfigMap leaderConfigMap = client.configMaps().inNamespace(NAMESPACE).withName("leader").get();
			if (leaderConfigMap != null && leaderConfigMap.getMetadata().getAnnotations() != null) {
				String leader = leaderConfigMap.getMetadata().getAnnotations().get("control-plane.alpha.kubernetes.io/leader");
				if (leader != null) {
					LOG.info("Found leader: {}", leader);
					return true;
				}
			}
			return false;
		});

		ConfigMap leaderConfigMap = client.configMaps().inNamespace(NAMESPACE).withName("leader").get();
		assertThat(leaderConfigMap).isNotNull();
		assertThat(leaderConfigMap.getMetadata().getAnnotations()).containsKey("control-plane.alpha.kubernetes.io/leader");
		String leaderPodName = leaderConfigMap.getMetadata().getAnnotations().get("control-plane.alpha.kubernetes.io/leader");
		leaderPodName = leaderPodName.split("_")[0];
		LOG.info("Leader pod name: {}", leaderPodName);

		io.fabric8.kubernetes.api.model.Pod leaderPod = client.pods().inNamespace(NAMESPACE).withName(leaderPodName).get();
		assertThat(leaderPod).isNotNull();
		assertThat(leaderPod.getStatus().getPhase()).isEqualTo("Running");
	}

	private static InputStream getConfigMap() {
		return util.inputStream("configmap.yaml");
	}

	private static InputStream getSecret() {
		return util.inputStream("secret.yaml");
	}

	private static InputStream getDeployment() {
		return util.inputStream("deployment.yaml");
	}

	private static InputStream getService() {
		return util.inputStream("service.yaml");
	}

	private static InputStream getServiceAccount() {
		return util.inputStream("sa.yaml");
	}

	private static InputStream getRole() {
		return util.inputStream("role.yaml");
	}

	private static InputStream getRolebinding() {
		return util.inputStream("rolebinding.yaml");
	}

	private static void deployManifests() {
		try {

			client.serviceAccounts().inNamespace(NAMESPACE).load(getServiceAccount()).create();
			client.rbac().roles().inNamespace(NAMESPACE).load(getRole()).create();
			client.rbac().roleBindings().inNamespace(NAMESPACE).load(getRolebinding()).create();
			client.configMaps().inNamespace(NAMESPACE).load(getConfigMap()).create();
			client.secrets().inNamespace(NAMESPACE).load(getSecret()).create();
			client.serviceAccounts().inNamespace(NAMESPACE).load(getServiceAccount()).create();
			client.apps().deployments().inNamespace(NAMESPACE).load(getDeployment()).create();

			Deployment deployment = client.apps().deployments().inNamespace(NAMESPACE).load(getDeployment()).get();
			Service service = client.services().inNamespace(NAMESPACE).load(getService()).get();

			util.createAndWait(NAMESPACE, null, deployment, service, true);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static void deleteManifests() {
	}
}
