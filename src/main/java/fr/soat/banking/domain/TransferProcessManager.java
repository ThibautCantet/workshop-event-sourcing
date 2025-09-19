package fr.soat.banking.domain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TransferProcessManager  {

    private final AccountRepository accountRepository;

    @Autowired
    public TransferProcessManager(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @EventListener
    public void on(TransferRequested transferRequested) {
        log.info("consuming {}", transferRequested.getClass().getSimpleName());
        Account receiverAccount = accountRepository.load(transferRequested.getReceiverAccountId());
        receiverAccount.credit(transferRequested.getAccountId(), transferRequested.getAmount());
        accountRepository.save(receiverAccount);
    }

    @EventListener
    public void on(CreditRequestRefused transferFundCredited) {
        Account senderAccount = accountRepository.load(transferFundCredited.getSourceAccountId());
        senderAccount.abortTransferRequest(transferFundCredited.getAccountId(), transferFundCredited.getAmount());
        accountRepository.save(senderAccount);
    }

    @EventListener
    public void on(FundCredited fundCredited) {
        log.info("consuming {}", fundCredited.getClass().getSimpleName());
        Account senderAccount = accountRepository.load(fundCredited.getSenderAccountId());
        senderAccount.debit(fundCredited.getAccountId(), fundCredited.getAmount());
        accountRepository.save(senderAccount);
    }

}
