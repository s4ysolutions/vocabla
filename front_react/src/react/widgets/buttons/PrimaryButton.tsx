import React from 'react'
import Button from './Button'

interface Props extends React.ButtonHTMLAttributes<HTMLButtonElement> {
    title?: string
}

const PrimaryButton: React.FC<Props> = ({ children, title, className, ...rest }) => (
    <Button className={`bg-blue-500 text-white hover:bg-blue-600 ${className || ''}`} {...rest}>
        {title}
        {children}
    </Button>
)

export default PrimaryButton