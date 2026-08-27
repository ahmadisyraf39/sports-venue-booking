# Scaling Demonstration Results

## Test setup
- Tool: Apache JMeter 5.6.3
- Load: 100 concurrent threads, 5s ramp-up, 10 iterations each (1000 requests)
- Target: POST /api/bookings via Gateway
- Test data: randomized across 11 courts, 11 days, 11 time slots

## Results

| Metric | 1 Replica | 3 Replicas | Improvement |
|---|---|---|---|
| Average response time | 31 ms | 7.84 ms | ~4x faster |
| Max response time | 555 ms | 62 ms | ~9x more consistent |
| Std. Dev. | 52.56 | 2.82 | Far more stable |
| Error % (409 conflicts) | 23.30% | 25.10% | Consistent (expected) |
| Throughput | 198.3/sec | 198.5/sec | Unchanged (JMeter-limited, not server-limited) |

## Correctness verification

After both runs, queried MongoDB directly for any court/date/time combination
booked more than once:
\`\`\`javascript
db.bookings.aggregate([
{ $group: { _id: { courtId: "$courtId", bookingDate: "$bookingDate", startTime: "$startTime" }, count: { $sum: 1 } } },
{ $match: { count: { $gt: 1 } } }
])
\`\`\`
Result: empty in both configurations — zero double-bookings occurred, confirming
the Redis-based lock coordinates correctly across multiple service instances,
not just within a single one.

## Conclusion

Scaling booking-service from 1 to 3 replicas measurably reduced both average
and worst-case latency under load, while maintaining identical correctness
guarantees — demonstrating that the distributed locking mechanism is genuinely
distributed, not an artifact of single-instance behavior.