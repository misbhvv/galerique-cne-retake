type Entry<T> = { data: T; expiry: number };

const store = new Map<string, Entry<unknown>>();

function get<T>(key: string): T | null {
	const entry = store.get(key) as Entry<T> | undefined;
	if (!entry || Date.now() > entry.expiry) {
		store.delete(key);
		return null;
	}
	return entry.data;
}

function set<T>(key: string, data: T, ttlMs: number): void {
	store.set(key, { data, expiry: Date.now() + ttlMs });
}

function remove(key: string): void {
	store.delete(key);
}

/**
 * Wraps a fetch call with an in-memory TTL cache keyed by `cacheKey`.
 * On a cache hit, returns a synthetic Response containing the cached JSON.
 * On a cache miss, executes the fetch; if ok, stores the parsed body and
 * returns a fresh synthetic Response so the caller can still call .json().
 */
async function fetchWithCache<T>(
	cacheKey: string,
	ttlMs: number,
	fetcher: () => Promise<Response>,
): Promise<Response> {
	const cached = get<T>(cacheKey);
	if (cached !== null) {
		return new Response(JSON.stringify(cached), {
			status: 200,
			headers: { "Content-Type": "application/json" },
		});
	}

	const res = await fetcher();
	if (res.ok) {
		const data: T = await res.json();
		set(cacheKey, data, ttlMs);
		return new Response(JSON.stringify(data), {
			status: 200,
			headers: { "Content-Type": "application/json" },
		});
	}
	return res;
}

export const clientCache = { get, set, remove, fetchWithCache };
