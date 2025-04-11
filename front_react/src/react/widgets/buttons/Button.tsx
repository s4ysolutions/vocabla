import React from 'react'

interface Props extends React.ButtonHTMLAttributes<HTMLButtonElement> {
    title?: string
}

const Button: React.FC<Props> = ({ children, title, className, ...rest }) => (
    <button className={`px-4 py-2 rounded ${className || ''}`} {...rest}>
        {title}
        {children}
    </button>
)

export default Button