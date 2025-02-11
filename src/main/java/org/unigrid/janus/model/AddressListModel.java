/*
	The Janus Wallet
	Copyright © 2022 The Unigrid Foundation

	This program is free software: you can redistribute it and/or modify it under the terms of the
	addended GNU Affero General Public License as published by the Free Software Foundation, version 3
	of the License (see COPYING and COPYING.addendum).

	This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
	even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
	GNU Affero General Public License for more details.

	You should have received an addended copy of the GNU Affero General Public License with this program.
	If not, see <http://www.gnu.org/licenses/> and <https://github.com/unigrid-project/janus-java>.
 */

package org.unigrid.janus.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.unigrid.janus.model.rpc.entity.ListAddressBalances;
import org.unigrid.janus.model.service.DebugService;

public class AddressListModel {
	private static DebugService debug = new DebugService();
	public static final String ADDRESS_LIST = "addressList";
	private static PropertyChangeSupport pcs;
	private static ObservableList<Address> addresses = FXCollections.observableArrayList();

	public AddressListModel() {
		if (this.pcs != null) {
			return;
		}
		this.pcs = new PropertyChangeSupport(this);
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		this.pcs.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		this.pcs.removePropertyChangeListener(listener);
	}

	public ObservableList<Address> getAddresses() {
		return this.addresses;
	}

	public void setAddresses(ListAddressBalances list) {
		int oldCount = 0;
		addresses.clear();
		int newCount = 0;
		for (Address g : list.getResult()) {
			debug.log(String.format("address: %s", g.getAddress()));
			addresses.add(g);
			newCount++;
		}
		this.pcs.firePropertyChange(this.ADDRESS_LIST, oldCount, newCount);
	}
}
