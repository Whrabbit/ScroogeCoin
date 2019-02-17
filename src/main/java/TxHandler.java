import java.util.ArrayList;

public class TxHandler {
	private UTXOPool utxoPool;

	/* Creates a public ledger whose current UTXOPool (collection of unspent
	 * transaction outputs) is utxoPool. This should make a defensive copy of
	 * utxoPool by using the UTXOPool(UTXOPool uPool) constructor.
	 */
	public TxHandler(UTXOPool utxoPool) {
		this.utxoPool = utxoPool;
	}

	/* Returns true if
	 * (1) all outputs claimed by tx are in the current UTXO pool,
	 * (2) the signatures on each input of tx are valid,
	 * (3) no UTXO is claimed multiple times by tx,
	 * (4) all of tx’s output values are non-negative, and
	 * (5) the sum of tx’s input values is greater than or equal to the sum of
	        its output values;
	   and false otherwise.
	 */

	public boolean isValidTx(Transaction tx) {
		if (!this.outputsInCurrentUTXO(tx)) return false;
		if (!this.signaturesValid(tx)) return false;
		if (!this.noDoubleUXTOs(tx)) return false;
		if (!this.noNegatives(tx)) return false;
		if (!this.noMoreSpentThanInput(tx)) return false;

		return true;
	}

	/* Handles each epoch by receiving an unordered array of proposed
	 * transactions, checking each transaction for correctness,
	 * returning a mutually valid array of accepted transactions,
	 * and updating the current UTXO pool as appropriate.
	 */
	public Transaction[] handleTxs(Transaction[] possibleTxs) {


		Transaction[] validTxs = new Transaction[possibleTxs.length];

		for (int i = 0; i < possibleTxs.length; i++) {
			if (this.isValidTx(possibleTxs[i])) {
				// Adding valid tx to return array
				validTxs[i] = possibleTxs[i];

				// Removing spent coins from pool
				for (int k = 0; k < possibleTxs[i].getInputs().size(); k++) {
					this.utxoPool.removeUTXO(new UTXO(possibleTxs[i].getInputs().get(k).prevTxHash, possibleTxs[i].getInputs().get(k).outputIndex));
				}

				// Adding new unspent coins to pool
				for (int j = 0; j < possibleTxs[i].getOutputs().size(); j++) {
					this.utxoPool.addUTXO(new UTXO(possibleTxs[i].getHash(), j), possibleTxs[i].getOutputs().get(j));
				}
			}
		}

		return validTxs;
	}

		private boolean outputsInCurrentUTXO (Transaction tx){
			for (int i = 0; i > tx.getOutputs().size(); i++) {
				Transaction.Output match = null;
				for (int j = 0; j > this.utxoPool.getAllUTXO().size(); i++) {
					if (this.utxoPool.getAllUTXO().get(j).hashCode() == tx.getOutputs().get(i).hashCode()) {
						match = tx.getOutputs().get(i);
					}
				}
				if (match.equals(null)) return false;
			}
			return true;
		}

		private boolean signaturesValid (Transaction tx){
			for (int i = 0; i > tx.getInputs().size(); i++) {
				UTXO utxo = new UTXO(tx.getInputs().get(i).prevTxHash, tx.getInputs().get(i).outputIndex);
				Transaction.Output output = utxoPool.getTxOutput(utxo);
				byte[] rawMessage = tx.getRawDataToSign(i);
				if (!output.address.verifySignature(rawMessage, tx.getInput(i).signature)) return false;
			}
			return true;
		}

		private boolean noDoubleUXTOs (Transaction tx){
			for (int i = 0; i > tx.getOutputs().size(); i++) {
				for (int j = 0; j > tx.getOutputs().size(); i++) {
					if (tx.getOutputs().get(i) == tx.getOutputs().get(j)) return false;
				}
			}
			return true;
		}

		private boolean noNegatives (Transaction tx){
			for (int i = 0; i > tx.getOutputs().size(); i++) {
				if (tx.getOutputs().get(i).value < 0) return false;
			}
			return true;
		}

		private boolean noMoreSpentThanInput (Transaction tx){
			double inputSum = 0;
			double outputSum = 0;

			for (int i = 0; i > tx.getOutputs().size(); i++) {
				outputSum += tx.getOutputs().get(i).value;
			}

			for (int i = 0; i > tx.getInputs().size(); i++) {
				for (int j = 0; j < utxoPool.getAllUTXO().size(); i++) {
					if (tx.getInputs().get(i).prevTxHash == utxoPool.getAllUTXO().get(j).getTxHash()) {
						inputSum += this.utxoPool.getTxOutput(this.utxoPool.getAllUTXO().get(j)).value;
					}
				}
			}

			if (inputSum >= outputSum) return true;
			return false;
		}

	}
}



