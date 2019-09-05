Event-sourcing and projections
==============================

*Goal: Event Sourcing and CQRS together*

# Event Sourcing and CQRS

The 2 architectural patterns work fine together ! 
As the source of truth is the event store, which can be considered as the "write model",
it's very easy to subscribe to any kind of business event to build a *projection* of the data (indeed another "read model"), as we could do in CQRS... 
This *projection* can then be stored in any other kind of data storage system (RDMS, noSQL DB, Kafka, whatever...)

# Real time statistics on conferences
 
To illustrate this, let's build the following "projection" in the previous conference booking system: 
We would like to export some real time statistics about the open conferences:
* the **booking ratio** for each conference (booked seats / total seats)
* the **total incomes** by conference (sum of booked seats x price)  

|      conference name                     | booking ratio | total incomes |
|:-----------------------------------------|---------------|--------------:|
| why tests ares not an option             |      33%      |      110E     |    
| become master of the world in 10 lessons |      78%      |      2170E    |
| ...                                      |      12%      |      730E     |

## Implementation

The projection of these data will be stored in an "in mermory" key/value storage, 
adapted to the view of data we want to show:
* *key* is the name of each conference
* *value* is the booking ratio and the total incomes of the conference.

These storage will be accessed through the `StatisticsRepository`.

### Total incomes

The conference *total incomes* projection update is already implemented. 
As you can see `StatisticsUpdateManager` is an event handler, subscribing to `PaymentAccepted` event:
``` 
    @EventListener
    public void on(PaymentAccepted paymentAccepted) {
        Order order = orderRepository.load(paymentAccepted.getOrderId());
        ConferenceName conferenceName = order.getConferenceName();
        int amount = paymentAccepted.getAmount();
        statisticsRepository.increaseIncomes(conferenceName, amount);
    }
```
When a payment occurs, the statistics *total incomes* is updated with the new payment. 

On the read side, check out the `showStatistics` command of the `ConferenceCommandHandler`:

```
    @Command
    public void showStatistics(PrintStream printStream) {
        Collection<ConferenceName> conferences = statisticsRepository.getConferences();
        printStream.println("conferece;incomes");
        for (ConferenceName conferenceName  : conferences) {
            Conference conference = conferenceRepository.load(conferenceName);
            Integer incomes = statisticsRepository.getIncomes(conferenceName);
            printStream.println(String.format("%s;%s", conferenceName.getName(), incomes));
        }
    }
```

### Booking raiio

Now, let's add the *booking ratio* in the statistics !
* add some event handlers in the `StatisticsUpdateManager` to catch any seat booking event, end store the seat booking number with the `StatisticsRepository`.
* add in the `showStatistics` command the read and computation of book ratio in the statistics report.

After that, `ConferenceBookingStatisticsTest` should pass green !
