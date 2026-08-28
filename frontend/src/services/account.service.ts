import { AccountCreateDto, AccountUpdateDto } from "../types";

const API_URL = process.env.NEXT_PUBLIC_BACKEND_URL;

const list = () => {
	return fetch(`${API_URL}/accounts`, {
		method: "GET",
		credentials: "include",
		headers: { "Content-Type": "application/json" },
	});
};

const getById = (id: string) => {
	return fetch(`${API_URL}/accounts/${id}`, {
		method: "GET",
		credentials: "include",
		headers: { "Content-Type": "application/json" },
	});
};

const me = () => {
	return fetch(`${API_URL}/accounts/me`, {
		method: "GET",
		credentials: "include",
		headers: { "Content-Type": "application/json" },
	});
};

const create = (data: AccountCreateDto) => {
	return fetch(`${API_URL}/accounts`, {
		method: "POST",
		credentials: "include",
		headers: { "Content-Type": "application/json" },
		body: JSON.stringify(data),
	});
};

const updateMe = (data: AccountUpdateDto) => {
	return fetch(`${API_URL}/accounts/me`, {
		method: "PUT",
		credentials: "include",
		headers: { "Content-Type": "application/json" },
		body: JSON.stringify(data),
	});
};

const getArtworks = (id: string) => {
	return fetch(`${API_URL}/accounts/${id}/artworks`, {
		method: "GET",
		credentials: "include",
		headers: { "Content-Type": "application/json" },
	});
};

const AccountService = {
	list,
	getById,
	me,
	create,
	updateMe,
	getArtworks,
};

export default AccountService;
