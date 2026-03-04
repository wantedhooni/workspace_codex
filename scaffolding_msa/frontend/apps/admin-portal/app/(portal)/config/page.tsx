"use client";

import { FormEvent, useEffect, useState } from "react";
import { AdminConfigQuery, ConfigEnvironment } from "@/lib/types";

const defaultQuery: AdminConfigQuery = {
    application: "application",
    profile: "default",
    label: "",
};

export default function ConfigPage() {
    const [query, setQuery] = useState<AdminConfigQuery>(defaultQuery);
    const [environment, setEnvironment] = useState<ConfigEnvironment | null>(null);
    const [error, setError] = useState<string | null>(null);

    const load = async (nextQuery: AdminConfigQuery) => {
        const params = new URLSearchParams({
            application: nextQuery.application,
            profile: nextQuery.profile,
        });

        if (nextQuery.label) {
            params.set("label", nextQuery.label);
        }

        const response = await fetch(`/api/config?${params.toString()}`, {
            cache: "no-store",
        });

        if (!response.ok) {
            throw new Error("config lookup failed");
        }

        return response.json() as Promise<ConfigEnvironment>;
    };

    useEffect(() => {
        load(defaultQuery)
            .then((payload) => {
                setEnvironment(payload);
                setError(null);
            })
            .catch(() => {
                setError("설정을 불러오지 못했습니다.");
            });
    }, []);

    const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
        event.preventDefault();

        try {
            const payload = await load(query);
            setEnvironment(payload);
            setError(null);
        } catch {
            setError("설정을 불러오지 못했습니다.");
        }
    };

    return (
        <section className="page-grid">
            <div className="hero-panel">
                <p className="eyebrow">Config Explorer</p>
                <h3>Config Server 원격 조회</h3>
                <p className="panel-subtitle">Vault와 native config-repo를 합친 결과를 현재 관리 화면에서 바로 확인할 수 있다.</p>
            </div>

            <div className="data-panel">
                <form className="config-toolbar" onSubmit={handleSubmit}>
                    <input
                        value={query.application}
                        onChange={(event) => setQuery((current) => ({ ...current, application: event.target.value }))}
                        placeholder="application"
                    />
                    <input
                        value={query.profile}
                        onChange={(event) => setQuery((current) => ({ ...current, profile: event.target.value }))}
                        placeholder="default"
                    />
                    <input
                        value={query.label}
                        onChange={(event) => setQuery((current) => ({ ...current, label: event.target.value }))}
                        placeholder="label"
                    />
                    <button type="submit">Load Config</button>
                </form>

                {error && <div className="empty-state">{error}</div>}

                {environment && (
                    <div className="config-sources">
                        {environment.propertySources.map((source) => (
                            <div key={source.name} className="config-source">
                                <h4>{source.name}</h4>
                                <table>
                                    <tbody>
                                        {Object.entries(source.source).map(([key, value]) => (
                                            <tr key={key}>
                                                <td>{key}</td>
                                                <td>
                                                    <code>{String(value)}</code>
                                                </td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                        ))}
                    </div>
                )}
            </div>
        </section>
    );
}

