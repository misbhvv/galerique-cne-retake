import { ArtworkCreateDto, ArtworkImageReorderRequestDto, ArtworkUpdateDto } from "../types";
import { clientCache } from "../lib/clientCache";

const API_URL = process.env.NEXT_PUBLIC_BACKEND_URL;

const TTL = {
	trending:       5 * 60_000,
	popularTags:   15 * 60_000,
	tagSuggestions: 3 * 60_000,
	likeCount:      1 * 60_000,
};

const list = (page: number = 0, size: number = 20, sort?: string) => {
	const params = new URLSearchParams({ page: String(page), size: String(size) });
	if (sort) params.append("sort", sort);

	return fetch(`${API_URL}/artworks?${params}`, {
		method: "GET",
		credentials: "include",
		cache: "no-store",
		headers: { "Content-Type": "application/json" },
	});
};

const getById = (id: string) => {
	return fetch(`${API_URL}/artworks/${id}`, {
		method: "GET",
		credentials: "include",
		cache: "no-store",
		headers: { "Content-Type": "application/json" },
	});
};

const search = (query: string) => {
	return fetch(`${API_URL}/artworks/search?query=${encodeURIComponent(query)}`, {
		method: "GET",
		credentials: "include",
		cache: "no-store",
		headers: { "Content-Type": "application/json" },
	});
};

const trending = () =>
	clientCache.fetchWithCache("trending", TTL.trending, () =>
		fetch(`${API_URL}/artworks/trending`, {
			method: "GET",
			credentials: "include",
			cache: "no-store",
			headers: { "Content-Type": "application/json" },
		}),
	);

const create = (data: ArtworkCreateDto) => {
	return fetch(`${API_URL}/artworks`, {
		method: "POST",
		credentials: "include",
		headers: { "Content-Type": "application/json" },
		body: JSON.stringify(data),
	});
};

const update = (id: string, data: ArtworkUpdateDto) => {
	return fetch(`${API_URL}/artworks/${id}`, {
		method: "PUT",
		credentials: "include",
		headers: { "Content-Type": "application/json" },
		body: JSON.stringify(data),
	});
};

const remove = (id: string) => {
	return fetch(`${API_URL}/artworks/${id}`, {
		method: "DELETE",
		credentials: "include",
	});
};

const listImages = (id: string) => {
	return fetch(`${API_URL}/artworks/${id}/images`, {
		method: "GET",
		credentials: "include",
		cache: "no-store",
		headers: { "Content-Type": "application/json" },
	});
};

const uploadImages = (id: string, files: File[]) => {
	const formData = new FormData();
	files.forEach((file) => formData.append("files", file));

	return fetch(`${API_URL}/artworks/${id}/images`, {
		method: "POST",
		credentials: "include",
		body: formData,
	});
};

const setMainImage = (id: string, imageId: string) => {
	return fetch(`${API_URL}/artworks/${id}/images/${imageId}/main`, {
		method: "PUT",
		credentials: "include",
	});
};

const reorderImages = (id: string, data: ArtworkImageReorderRequestDto) => {
	return fetch(`${API_URL}/artworks/${id}/images/order`, {
		method: "PUT",
		credentials: "include",
		headers: { "Content-Type": "application/json" },
		body: JSON.stringify(data),
	});
};

const deleteImage = (id: string, imageId: string) => {
	return fetch(`${API_URL}/artworks/${id}/images/${imageId}`, {
		method: "DELETE",
		credentials: "include",
	});
};

const like = (id: string) => {
	clientCache.remove(`likeCount:${id}`);
	return fetch(`${API_URL}/artworks/${id}/like`, {
		method: "POST",
		credentials: "include",
	});
};

const unlike = (id: string) => {
	clientCache.remove(`likeCount:${id}`);
	return fetch(`${API_URL}/artworks/${id}/like`, {
		method: "DELETE",
		credentials: "include",
	});
};

const getLikeCount = (id: string) =>
	clientCache.fetchWithCache(`likeCount:${id}`, TTL.likeCount, () =>
		fetch(`${API_URL}/artworks/${id}/likes`, {
			method: "GET",
			credentials: "include",
			cache: "no-store",
			headers: { "Content-Type": "application/json" },
		}),
	);

const searchByTag = (tag: string) => {
	return fetch(`${API_URL}/artworks/search/tag?tag=${encodeURIComponent(tag)}`, {
		method: "GET",
		credentials: "include",
		cache: "no-store",
		headers: { "Content-Type": "application/json" },
	});
};

const tagSuggestions = (query: string = "") => {
	const trimmed = query.trim();
	const params = new URLSearchParams();
	if (trimmed) params.append("query", trimmed);
	return clientCache.fetchWithCache(`tagSuggestions:${trimmed}`, TTL.tagSuggestions, () =>
		fetch(`${API_URL}/tags/suggestions?${params.toString()}`, {
			method: "GET",
			credentials: "include",
			cache: "no-store",
			headers: { "Content-Type": "application/json" },
		}),
	);
};

const popularTags = (limit: number = 10) => {
	const params = new URLSearchParams({ limit: String(limit) });
	return clientCache.fetchWithCache(`popularTags:${limit}`, TTL.popularTags, () =>
		fetch(`${API_URL}/tags/popular?${params.toString()}`, {
			method: "GET",
			credentials: "include",
			cache: "no-store",
			headers: { "Content-Type": "application/json" },
		}),
	);
};

const ArtworkService = {
	list,
	getById,
	search,
	searchByTag,
	tagSuggestions,
	popularTags,
	trending,
	create,
	update,
	remove,
	listImages,
	uploadImages,
	setMainImage,
	reorderImages,
	deleteImage,
	like,
	unlike,
	getLikeCount,
};

export default ArtworkService;
