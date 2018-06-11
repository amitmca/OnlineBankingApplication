package com.userfront.service.UserServiceImpl;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.userfront.dao.PrimaryAccountDao;
import com.userfront.dao.PrimaryTransactionDao;
import com.userfront.dao.RecipientDao;
import com.userfront.dao.SavingsAccountDao;
import com.userfront.dao.SavingsTransactionDao;
import com.userfront.domain.PrimaryAccount;
import com.userfront.domain.PrimaryTransaction;
import com.userfront.domain.Recipient;
import com.userfront.domain.SavingsAccount;
import com.userfront.domain.SavingsTransaction;
import com.userfront.domain.User;
import com.userfront.service.TransactionService;
import com.userfront.service.UserService;


@Service
public class TransactionServiceImpl implements TransactionService {
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private PrimaryTransactionDao primaryTransactionDao;
	
	@Autowired
	private SavingsTransactionDao savingsTransactionDao;
	
	@Autowired
	private PrimaryAccountDao primaryAccountDao;
	
	@Autowired
	private SavingsAccountDao savingsAccountDao;
	
	@Autowired
	private RecipientDao recipientDao;

	public List<PrimaryTransaction> findPrimaryTransactionList(String username){
		User user = userService.findByUsername(username);
		List<PrimaryTransaction> primaryTransactionList = user.getPrimaryAccount().getPrimaryTransactionList();
		return primaryTransactionList;
	}
	
	public List<SavingsTransaction> findSavingsTransactionList(String username){
		User user = userService.findByUsername(username);
		List<SavingsTransaction> savingsTransactionList = user.getSavingsAccount().getSavingsTransactionList();
		return savingsTransactionList;
	}
	
	public void savePrimaryDepositTransaction(PrimaryTransaction primaryTransaction) {
		primaryTransactionDao.save(primaryTransaction);
	}
	
	public void saveSavingsDepositTransaction(SavingsTransaction savingsTransaction) {
		savingsTransactionDao.save(savingsTransaction);
	}

	
	public void savePrimaryWithdrawTransaction(PrimaryTransaction primaryTransaction) {
		primaryTransactionDao.save(primaryTransaction);
	}

	
	public void saveSavingsWithdrawTransaction(SavingsTransaction savingsTransaction) {
		savingsTransactionDao.save(savingsTransaction);
	}

	
	public void betweenAccountsTransfer(String transferFrom, String transferTo, String amount,
			PrimaryAccount primaryAccount, SavingsAccount savingsAccount) throws Exception {
		if(transferFrom.equalsIgnoreCase("Primary") && transferTo.equalsIgnoreCase("Savings")) {
			primaryAccount.setAccountBalance(primaryAccount.getAccountBalance().subtract(new BigDecimal(amount)));
			savingsAccount.setAccountBalance(savingsAccount.getAccountBalance().add(new BigDecimal(amount)));
			
			primaryAccountDao.save(primaryAccount);
			savingsAccountDao.save(savingsAccount);
			
			PrimaryTransaction primaryTransaction = new PrimaryTransaction(new Date(), "Transfer from primary to savings", "Transfer", "Finished", Double.parseDouble(amount), primaryAccount.getAccountBalance(), primaryAccount);
			primaryTransactionDao.save(primaryTransaction);
			SavingsTransaction savingsTransaction = new SavingsTransaction(new Date(), "Transfered from primary to savings", "Transfer", "Finished", Double.parseDouble(amount), savingsAccount.getAccountBalance(), savingsAccount);
			savingsTransactionDao.save(savingsTransaction);
		}
		else if(transferFrom.equalsIgnoreCase("Savings") && transferTo.equalsIgnoreCase("Primary")){
			primaryAccount.setAccountBalance(primaryAccount.getAccountBalance().add(new BigDecimal(amount)));
			savingsAccount.setAccountBalance(savingsAccount.getAccountBalance().subtract(new BigDecimal(amount)));
			
			primaryAccountDao.save(primaryAccount);
			savingsAccountDao.save(savingsAccount);
			
			PrimaryTransaction primaryTransaction = new PrimaryTransaction(new Date(), "Transfer from primary to savings", "Transfer", "Finished", Double.parseDouble(amount), primaryAccount.getAccountBalance(), primaryAccount);
			primaryTransactionDao.save(primaryTransaction);
			SavingsTransaction savingsTransaction = new SavingsTransaction(new Date(), "Transfered from primary to savings", "Transfer", "Finished", Double.parseDouble(amount), savingsAccount.getAccountBalance(), savingsAccount);
			savingsTransactionDao.save(savingsTransaction);
		}
		else {
			throw new Exception("Invalid Transfer");
		}
	}

	
	public void saveRecipient(Recipient recipient) {
		recipientDao.save(recipient);
	}

	
	public Recipient findRecipientByName(String recipientName) {
		 return recipientDao.findByName(recipientName);
	}

	
	public List<Recipient> findRecipientList(Principal principal) {
		String username = principal.getName();
		List<Recipient> recipientList = ((Collection<Recipient>) recipientDao.findAll()).stream()
				.filter(recipient -> username.equals(recipient.getUser().getUsername()))
				.collect(Collectors.toList());
		
		return recipientList;
	}

	public void deleteRecipientByName(String recipientName) {
		recipientDao.deleteByName(recipientName);
	}
}
