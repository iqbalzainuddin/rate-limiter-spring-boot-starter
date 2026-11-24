-- Token bucket check-and-decrement.
-- KEYS[1] = bucket key
-- ARGV[1] = capacity
-- ARGV[2] = refill per second
-- ARGV[3] = now (epoch millis)
-- ARGV[4] = requested tokens (usually 1)
-- Returns: {allowed (0/1), remaining (tokens), retry_after (seconds)}

local key = KEYS[1]
local capacity = tonumber(ARGV[1])
local refill_rate = tonumber(ARGV[2])
local now = tonumber(ARGV[3])
local requested = tonumber(ARGV[4])

local bucket = redis.call('HMGET', key, 'tokens', 'last_refill')
local tokens = tonumber(bucket[1])
local last_refill = tonumber(bucket[2])

if tokens == nil then
    tokens = capacity
    last_refill = now
end

local elapsed_ms = now - last_refill
if elapsed_ms > 0 then
    tokens = math.min(capacity, tokens + (elapsed_ms / 1000.0) * refill_rate)
    last_refill = now
end

local allowed = 0
local remaining = tokens
local retry_after = 0

if tokens >= requested then
    tokens = tokens - requested
    allowed = 1
    remaining = tokens
else
    local deficit = requested - tokens
    if refill_rate > 0 then
        retry_after = math.ceil(deficit / refill_rate)
    else
        retry_after = 86400
    end
    if retry_after < 1 then
        retry_after = 1
    end
end

redis.call('HMSET', key, 'tokens', tokens, 'last_refill', last_refill)
redis.call('EXPIRE', key, 3600)

return {allowed, remaining, retry_after}
