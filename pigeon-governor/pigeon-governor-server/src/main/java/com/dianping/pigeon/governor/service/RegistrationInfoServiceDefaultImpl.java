package com.dianping.pigeon.governor.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dianping.pigeon.registry.util.Constants;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;

import com.dianping.pigeon.registry.Registry;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.registry.exception.RegistryException;
import com.dianping.pigeon.registry.util.Utils;
import com.dianping.pigeon.registry.zookeeper.CuratorClient;
import com.dianping.pigeon.registry.zookeeper.CuratorRegistry;
import com.dianping.pigeon.remoting.provider.config.annotation.Service;

@Service
public class RegistrationInfoServiceDefaultImpl implements RegistrationInfoService {

	private Map<String, Registry> registryMap = Maps.newHashMap();

	private Registry getRegistry(String name) throws RegistryException {
		Registry registry = registryMap.get(name);

		if (registry != null) {
			return registry;
		} else {
			for (Registry _registry : RegistryManager.getInstance().getRegistryList()) {
				if(_registry.getName().equals(name)) {
					registryMap.put(name, _registry);
					return _registry;
				}
			}
		}

		throw new RegistryException("no registry found: " + name);
	}

	@Override
	public String getAppOfService(String url, String group) throws RegistryException {
		String serviceAddress = getRegistry(Constants.REGISTRY_CURATOR_NAME).getServiceAddress(url, group, false);
		List<String> addressList = Utils.getAddressList(url, serviceAddress);
		Map<String, Integer> appCount = new HashMap<String, Integer>();
		for (String addr : addressList) {
			String addrApp = getRegistry(Constants.REGISTRY_CURATOR_NAME).getServerApp(addr);
			if (StringUtils.isNotBlank(addrApp)) {
				Integer count = appCount.get(addrApp);
				if (count == null) {
					appCount.put(addrApp, 1);
				} else {
					return addrApp;
				}
			}
		}
		if (!appCount.isEmpty()) {
			return appCount.keySet().iterator().next();
		}
		return null;
	}

	public String getAppOfService(String url) throws RegistryException {
		return getAppOfService(url, null);
	}

	@Override
	public String getWeightOfAddress(String address) throws RegistryException {
		CuratorClient client = ((CuratorRegistry) getRegistry(Constants.REGISTRY_CURATOR_NAME)).getCuratorClient();
		try {
			return client.get("/DP/WEIGHT/" + address);
		} catch (Exception e) {
			throw new RegistryException(e);
		}
	}

	public String getValueOfPath(String path) throws RegistryException {
		CuratorClient client = ((CuratorRegistry) getRegistry(Constants.REGISTRY_CURATOR_NAME)).getCuratorClient();
		try {
			return client.get(path);
		} catch (Exception e) {
			throw new RegistryException(e);
		}
	}

	@Override
	public String getAppOfAddress(String address) throws RegistryException {
		return getRegistry(Constants.REGISTRY_CURATOR_NAME).getServerApp(address);
	}

	@Override
	public List<String> getAddressListOfService(String url, String group) throws RegistryException {
		String serviceAddress = getRegistry(Constants.REGISTRY_CURATOR_NAME).getServiceAddress(url, group, false);
		List<String> addressList = Utils.getAddressList(url, serviceAddress);
		return addressList;
	}

	@Override
	public List<String> getAddressListOfService(String url) throws RegistryException {
		return getAddressListOfService(url, null);
	}
}
