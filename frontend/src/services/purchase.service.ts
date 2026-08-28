const API_URL = process.env.NEXT_PUBLIC_BACKEND_URL;

const purchase = (artworkId: string) => {
	return fetch(`${API_URL}/purchases/${artworkId}`, {
		method: "POST",
		credentials: "include",
		headers: { "Content-Type": "application/json" },
	});
};

const myPurchases = () => {
	return fetch(`${API_URL}/purchases/me`, {
		method: "GET",
		credentials: "include",
		headers: { "Content-Type": "application/json" },
	});
};

const PurchaseService = {
	purchase,
	myPurchases,
};

export default PurchaseService;
