/*
    The Janus Wallet
    Copyright © 2021 The Unigrid Foundation

    This program is free software: you can redistribute it and/or modify it under the terms of the
    addended GNU Affero General Public License as published by the Free Software Foundation, version 3
    of the License (see COPYING and COPYING.addendum).

    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
    even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU Affero General Public License for more details.

    You should have received an addended copy of the GNU Affero General Public License with this program.
    If not, see <http://www.gnu.org/licenses/> and <https://github.com/unigrid-project/janus-java>.
*/

package org.unigrid.janus.model.service;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import javax.naming.ConfigurationException;
import lombok.SneakyThrows;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.unigrid.janus.model.DataDirectory;
import org.unigrid.janus.model.Preferences;
import org.unigrid.janus.model.User;
import org.unigrid.janus.model.rpc.JsonConfiguration;
import org.unigrid.janus.model.rpc.entity.BaseResult;
import jakarta.ws.rs.core.Response;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Timer;

@ApplicationScoped
public class RPCService {
	private static final String PROPERTY_USERNAME_KEY = "janus.rpc.username";
	private static final String PROPERTY_USERNAME = Preferences.PROPS.getString(PROPERTY_USERNAME_KEY);
	private static final String PROPERTY_PASSWORD_KEY = "janus.rpc.password";
	private static final String PROPERTY_PASSWORD = Preferences.PROPS.getString(PROPERTY_PASSWORD_KEY);

	private static Timer pollingTimer;

	private User credentials;
	@Inject private Daemon daemon;

	private static WebTarget target;

	private String getRPCProperty(Configuration config, String key, String value, String dataDirConfig,
	                              boolean randomizeOnMissingProperty) throws ConfigurationException {
		if (StringUtils.isNotBlank(value)) {
			return value;
		}

		String propertyValue = config.getString(dataDirConfig);

		if (propertyValue == null) {
			if (randomizeOnMissingProperty) {
				propertyValue = RandomStringUtils.randomAlphabetic(40);
			} else {
				throw new ConfigurationException(String.format("Username for RPC endpoint not "
					+ "found in either '%s' property or the configuration file '%s'.",
					key, DataDirectory.getConfigFile().getAbsolutePath())
				);
			}
		}

		return propertyValue;
	}

	@PostConstruct @SneakyThrows
	private void init() {
		if (target != null) {
			return;
		}

		Configuration config = DataDirectory.getConfig();
		credentials = new User();

		credentials.setName(getRPCProperty(config, PROPERTY_USERNAME_KEY, PROPERTY_USERNAME,
		                    DataDirectory.DATADIR_CONFIG_RPCUSER_KEY, daemon.isLocalFile()));

		credentials.setPassword(getRPCProperty(config, PROPERTY_PASSWORD_KEY, PROPERTY_PASSWORD,
		                        DataDirectory.DATADIR_CONFIG_RPCPASSWORD_KEY, daemon.isLocalFile()));

		target = ClientBuilder.newBuilder()
			.register(new JsonConfiguration())
			.register(HttpAuthenticationFeature.basic(credentials.getName(), credentials.getPassword()))
			.build().target(daemon.getRPCAdress());
	}

	public void pollForInfo(int interval) {
		pollingTimer = new Timer(true);
		pollingTimer.scheduleAtFixedRate(new PollingTask(), 0, interval);
	}

	public void stopPolling() {
		if (pollingTimer != null) {
			pollingTimer.cancel();
			pollingTimer.purge();
		}
	}

	public <R, T> T call(R request, Class<T> clazz) {
		return target.request().post(Entity.json(request)).readEntity(clazz);
	}

	public <R> String callToJson(R request) {
		Response r = target.request().post(Entity.json(request));
		String result = "{}";
		if (r.hasEntity()) {
			InputStream instream = (InputStream) r.getEntity();
			Jsonb jsonb = JsonbBuilder.create();
			String sRequest = jsonb.toJson(request);
			result = String.format("RPC call: %s\n"
				                 + "Response: %s",
				                   sRequest,
				                   convertStreamToString(instream));
		}
		return result;
	}

	public String resultToJson(BaseResult result) {
		Jsonb jsonb = JsonbBuilder.create();
		return jsonb.toJson(result);
	}

	public <R> String alert(R request) {
		String result = callToJson(request);
		Alert a = new Alert(AlertType.INFORMATION,
							result,
							ButtonType.OK);
		a.showAndWait();
		return result;
	}

	private static String convertStreamToString(InputStream is) {

		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}
}
