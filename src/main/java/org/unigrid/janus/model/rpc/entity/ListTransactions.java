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

package org.unigrid.janus.model.rpc.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.unigrid.janus.model.Transaction;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class ListTransactions extends BaseResult<List<ListTransactions.Result>> {
	private static final String METHOD = "listtransactions";

	public static class Request extends BaseRequest {
		public Request(int offset, int count) {
			super(METHOD);
			this.setParams(new Object[]{"*", count, offset, true});
		}
	}

	@Data
	public static class Result extends Transaction {
	}
}
