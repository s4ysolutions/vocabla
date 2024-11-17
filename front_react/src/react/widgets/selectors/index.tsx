import React, { ReactElement } from 'react'

interface Props {
    values: Array<{ id: string; value: string }>
    selectedId: string
    onChange?: (id: string) => void
}

const Selector: React.FC<Props> = ({
    values,
    selectedId,
    onChange, }): ReactElement =>
    <div className="relative inline-block">
        <select
            value={selectedId}
            onChange={(e) => onChange && onChange(e.target.value)}
            className="block w-auto px-4 py-2 bg-white border border-gray-300 rounded shadow focus:outline-none focus:ring-2 focus:ring-blue-500"
        >
            {values.map((e) => (
                <option key={e.id} value={e.id}>
                    {e.value}
                </option>
            ))}
        </select>
    </div>

export default Selector