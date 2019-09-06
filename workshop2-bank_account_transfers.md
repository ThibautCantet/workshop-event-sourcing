Event-sourcing and aggregate messages
=====================================

*Goal: Understand how aggregates can communicate together in a simple business process.*
      
## Transfer from an account to another
We are going to implement the *Transfer* feature. The *Transfer* process implies a *sender account* and a *receiver account*. 
The given *amount* should be deduced from *sender account* and added to the *receiver account* 
### Succeeding transfer
```gherkin
Given a sender account "A" and a receiver account "B"
And "A" has a balance of 200
And "B" has a balance of 0
When I transfer 50 from "A" to "B"
Then "A" balance is 150
Then "B" balance is 50
```
### Transfer with insufficient funds
```gherkin
Given a sender account "A" and a receiver account "B"
And "A" has a balance of 10
And "B" has a balance of 0
When I transfer 50 from "A" to "B"
Then "A" balance is 10
Then "B" balance is 0
```
### Transfer to a closed account
```gherkin
Given a sender account "A" and a receiver account "B"
And "A" has a balance of 200
And "B" is closed
When I transfer 50 from "A" to "B"
Then "A" balance is 200
```

## Implementation

The `transfer` command needs to orchestrate 2 actions:
1. remove amount from source account.
2. add amount to target account. 

As the business logic orchestration is pretty simple, it's (partly) implemented it in the `Account` aggregate *decision functions*:
* `Account requestTransfer(Account targetAccount, int amount)`
* `Account receiveTransfer(Account sourceAccount, int amount)`

### Evolution functions

Implement the missing *evolution functions*:
```
    @EvolutionFunction
    void apply(TransferSent event) {
        //FIXME
        ...
    }

    @EvolutionFunction
    void apply(TransferReceived event) {
        //FIXME
        ...
    }
```
The test `AccountTest.should_succeed_to_transfer()` should then pass green !

### How to manage failures ?

When something wrong is requested (e.g. withraw from a closed account), 
we decided until now to implement the failure with an exception.

Sometimes, it will be interesting to track record the request failure with an event (for audit, or when the process is a *long process*).

Fix the `requestTransfer()` to raise a `TransferRefused` event when the amount is higher than the balance of source account.
The test `AccountTest.should_fail_to_transfer_when_funds_are_insuffisent()` should then pass green !
