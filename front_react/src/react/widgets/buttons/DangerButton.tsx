import React, { ReactElement, ReactNode } from 'react'

interface Props {
    title?: string
    children?: ReactNode
    onClick?: () => void
}

const DangerButton: React.FC<Props> = ({children, title}): ReactElement =>
    <button className="px-4 py-2 bg-red-500 text-white rounded hover:bg-red-600">
        {title}
        {children}
    </button>

export default DangerButton