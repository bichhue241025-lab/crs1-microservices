import { useCallback, useEffect, useState } from 'react';
import axios from 'axios';

import {
    getApiKeys,
    createApiKey,
    revokeApiKey
} from '../api/apiKeyApi';

import type { ApiKey } from '../types/apiKey';
import type { ApiErrorResponse } from '../types/apiError';

export default function ApiKeysPage() {

    const [keys, setKeys] = useState<ApiKey[]>([]);
    const [loading, setLoading] = useState(true);

    const [ownerName, setOwnerName] = useState('');
    const [scopes, setScopes] = useState('courses:read');
    const [validDays, setValidDays] = useState('30');

    const [newKeyValue, setNewKeyValue] =
        useState<string | null>(null);

    const [error, setError] =
        useState<string | null>(null);

    const loadKeys = useCallback(() => {

        setLoading(true);

        getApiKeys()
            .then((res) => {
                setKeys(res.data);
            })
            .catch(() => {
                setError(
                    'Khong tai duoc danh sach API Key.'
                );
            })
            .finally(() => {
                setLoading(false);
            });

    }, []);

    useEffect(() => {
        void loadKeys();
    }, [loadKeys]);

    const handleCreate = async (
        e: React.FormEvent
    ) => {

        e.preventDefault();

        setError(null);
        setNewKeyValue(null);

        try {

            const res = await createApiKey({
                ownerName,
                scopes,
                validDays:
                    validDays
                        ? Number(validDays)
                        : undefined
            });

            setNewKeyValue(
                res.data.keyValue
            );

            setOwnerName('');

            await loadKeys();

        } catch (err) {

            if (
                axios.isAxiosError<ApiErrorResponse>(err) &&
                err.response?.data?.message
            ) {

                setError(
                    err.response.data.message
                );

            } else {

                setError(
                    'Cap API Key khong thanh cong.'
                );
            }
        }
    };

    const handleRevoke = async (
        key: ApiKey
    ) => {

        const confirmed = window.confirm(
            `Thu hoi API Key cua "${key.ownerName}"?`
        );

        if (!confirmed) {
            return;
        }

        try {

            await revokeApiKey(key.id);

            await loadKeys();

        } catch {

            alert(
                'Thu hoi API Key khong thanh cong.'
            );
        }
    };

    return (
        <div
            style={{
                padding: 24,
                maxWidth: 900,
                margin: '0 auto'
            }}
        >

            <h1>
                Quan ly API Key doi tac
            </h1>

            <form
                onSubmit={handleCreate}
                style={{
                    border: '1px solid #ddd',
                    padding: 16,
                    borderRadius: 8,
                    marginBottom: 24
                }}
            >

                <h3>
                    Cap API Key moi
                </h3>

                <div
                    style={{
                        marginBottom: 12
                    }}
                >
                    <label>
                        Ten doi tac
                    </label>

                    <br />

                    <input
                        value={ownerName}
                        onChange={(e) =>
                            setOwnerName(
                                e.target.value
                            )
                        }
                        required
                    />
                </div>

                <div
                    style={{
                        marginBottom: 12
                    }}
                >
                    <label>
                        Scopes
                    </label>

                    <br />

                    <input
                        value={scopes}
                        onChange={(e) =>
                            setScopes(
                                e.target.value
                            )
                        }
                        required
                    />
                </div>

                <div
                    style={{
                        marginBottom: 12
                    }}
                >
                    <label>
                        Hieu luc
                        (so ngay, de trong = vinh vien)
                    </label>

                    <br />

                    <input
                        type="number"
                        value={validDays}
                        onChange={(e) =>
                            setValidDays(
                                e.target.value
                            )
                        }
                    />
                </div>

                {error && (
                    <p
                        style={{
                            color: '#b91c1c'
                        }}
                    >
                        {error}
                    </p>
                )}

                <button type="submit">
                    Cap API Key
                </button>

            </form>

            {newKeyValue && (

                <div
                    style={{
                        background: '#fef9c3',
                        padding: 12,
                        borderRadius: 8,
                        marginBottom: 24
                    }}
                >

                    <strong>
                        Key vua tao
                        (hay luu lai ngay):
                    </strong>

                    <pre
                        style={{
                            userSelect: 'all',
                            whiteSpace: 'pre-wrap',
                            wordBreak: 'break-all'
                        }}
                    >
                        {newKeyValue}
                    </pre>

                </div>
            )}

            {loading ? (

                <p>
                    Dang tai...
                </p>

            ) : (

                <table
                    style={{
                        width: '100%',
                        borderCollapse: 'collapse'
                    }}
                >

                    <thead>
                    <tr
                        style={{
                            textAlign: 'left',
                            borderBottom:
                                '2px solid #333'
                        }}
                    >

                        <th>Doi tac</th>
                        <th>Scopes</th>
                        <th>Trang thai</th>
                        <th>Het han</th>
                        <th>Thao tac</th>

                    </tr>
                    </thead>

                    <tbody>

                    {keys.map((k) => (

                        <tr
                            key={k.id}
                            style={{
                                borderBottom:
                                    '1px solid #eee'
                            }}
                        >

                            <td>
                                {k.ownerName}
                            </td>

                            <td>
                                {k.scopes}
                            </td>

                            <td
                                style={{
                                    color:
                                        k.status === 'ACTIVE'
                                            ? '#15803d'
                                            : '#b91c1c'
                                }}
                            >
                                {k.status}
                            </td>

                            <td>
                                {
                                    k.expiresAt
                                        ? new Date(
                                            k.expiresAt
                                        )
                                            .toLocaleDateString(
                                                'vi-VN'
                                            )
                                        : 'Vinh vien'
                                }
                            </td>

                            <td>

                                {
                                    k.status === 'ACTIVE' && (

                                        <button
                                            onClick={() =>
                                                handleRevoke(k)
                                            }
                                        >
                                            Thu hoi
                                        </button>
                                    )
                                }

                            </td>

                        </tr>
                    ))}

                    </tbody>

                </table>
            )}

        </div>
    );
}