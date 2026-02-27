import { DataProvider, fetchUtils } from 'react-admin';

const API_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api';

const httpClient = (url: string, options: fetchUtils.Options = {}) => {
  const token = localStorage.getItem('tm_token');
  const headers = new Headers(options.headers || { Accept: 'application/json' });
  if (token) {
    headers.set('Authorization', `Bearer ${token}`);
  }
  if (!headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }
  return fetchUtils.fetchJson(url, { ...options, headers });
};

export const dataProvider: DataProvider = {
  getList: async (resource) => {
    const { json } = await httpClient(`${API_URL}/${resource}`);
    return { data: json, total: json.length };
  },
  getOne: async (resource, params) => {
    const { json } = await httpClient(`${API_URL}/${resource}/${params.id}`);
    return { data: json };
  },
  getMany: async (resource, params) => {
    const { json } = await httpClient(`${API_URL}/${resource}`);
    const data = json.filter((item: any) => params.ids.includes(item.id));
    return { data };
  },
  getManyReference: async (resource, params) => {
    const { json } = await httpClient(`${API_URL}/${resource}`);
    const data = json.filter((item: any) => item[params.target] === params.id);
    return { data, total: data.length };
  },
  create: async (resource, params) => {
    const { json } = await httpClient(`${API_URL}/${resource}`, {
      method: 'POST',
      body: JSON.stringify(params.data),
    });
    return { data: json };
  },
  update: async (resource, params) => {
    const { json } = await httpClient(`${API_URL}/${resource}/${params.id}`, {
      method: 'PUT',
      body: JSON.stringify(params.data),
    });
    return { data: json };
  },
  updateMany: async (resource, params) => {
    const results = await Promise.all(
      params.ids.map((id) =>
        httpClient(`${API_URL}/${resource}/${id}`, {
          method: 'PUT',
          body: JSON.stringify(params.data),
        })
      )
    );
    return { data: results.map((res) => res.json.id) };
  },
  delete: async (resource, params) => {
    await httpClient(`${API_URL}/${resource}/${params.id}`, { method: 'DELETE' });
    return { data: params.previousData };
  },
  deleteMany: async (resource, params) => {
    await Promise.all(
      params.ids.map((id) => httpClient(`${API_URL}/${resource}/${id}`, { method: 'DELETE' }))
    );
    return { data: params.ids };
  },
};
